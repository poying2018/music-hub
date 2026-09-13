package dev.naominet.lazer.core

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import java.io.BufferedInputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.Locale
import java.util.concurrent.Executors

/**
 * LAN upload server for the internal library: the user opens `http://<phone-ip>:8765` in any
 * browser on the same WiFi and drops audio files onto the page. Files arrive as raw POST bodies
 * at `/upload/<urlencoded-name>` and go straight into [InternalMusicLibrary]; nothing is written
 * outside the app's private storage.
 *
 * Uses hand-rolled HTTP over a ServerSocket with explicit Content-Length streaming into private
 * cache storage before committing to [InternalMusicLibrary].
 */
internal class WifiTransferServer(
    context: Context,
    private val onImported: (fileName: String) -> Unit,
    private val port: Int = PORT,
) {
    private val appContext = context.applicationContext
    private val executor = Executors.newFixedThreadPool(4)
    @Volatile private var running = false
    private var serverSocket: ServerSocket? = null

    private val wifiLock: WifiManager.WifiLock? by lazy {
        runCatching {
            val wm = appContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            wm?.createWifiLock(WifiManager.WIFI_MODE_FULL_HIGH_PERF, "lazer:wifi_transfer")?.apply {
                setReferenceCounted(false)
            }
        }.getOrNull()
    }

    private val wakeLock: PowerManager.WakeLock? by lazy {
        runCatching {
            val pm = appContext.getSystemService(Context.POWER_SERVICE) as? PowerManager
            pm?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lazer:wifi_transfer_wake")?.apply {
                setReferenceCounted(false)
            }
        }.getOrNull()
    }

    @Throws(java.io.IOException::class)
    fun start() {
        if (running) return
        val socket = ServerSocket()
        socket.reuseAddress = true
        socket.bind(InetSocketAddress("0.0.0.0", port))
        serverSocket = socket
        running = true

        runCatching { wifiLock?.acquire() }
        runCatching { wakeLock?.acquire(30 * 60 * 1000L) }

        Thread({
            try {
                while (running) {
                    val client = socket.accept()
                    executor.execute { handleClient(client) }
                }
            } catch (_: Throwable) {
                // accept() throws on close(); the loop exits normally.
            } finally {
                runCatching { socket.close() }
            }
        }, "wifi-transfer").apply { isDaemon = true; start() }
    }

    fun stop() {
        running = false
        runCatching { serverSocket?.close() }
        serverSocket = null
        runCatching { if (wifiLock?.isHeld == true) wifiLock?.release() }
        runCatching { if (wakeLock?.isHeld == true) wakeLock?.release() }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.use { client ->
                client.setSoTimeout(30_000)
                val input = BufferedInputStream(client.getInputStream())
                val requestLine = readLine(input) ?: return
                val parts = requestLine.split(" ")
                if (parts.size < 2) return
                val method = parts[0].uppercase(Locale.ROOT)
                val rawPath = parts[1]
                val headers = mutableMapOf<String, String>()
                while (true) {
                    val line = readLine(input) ?: break
                    if (line.isEmpty()) break
                    val colon = line.indexOf(':')
                    if (colon > 0) {
                        headers[line.substring(0, colon).trim().lowercase(Locale.ROOT)] =
                            line.substring(colon + 1).trim()
                    }
                }
                val pathOnly = rawPath.substringBefore('?')
                val contentLength = headers["content-length"]?.toLongOrNull() ?: 0L

                when {
                    method == "OPTIONS" ->
                        respond(client, 204, "text/plain", ByteArray(0))
                    method == "GET" && !pathOnly.startsWith(UPLOAD_PATH) ->
                        respond(client, 200, "text/html; charset=utf-8", UPLOAD_PAGE.toByteArray(Charsets.UTF_8))
                    method == "POST" && pathOnly.startsWith(UPLOAD_PATH) ->
                        handleUpload(client, input, pathOnly, contentLength)
                    else ->
                        respond(client, 404, "text/plain; charset=utf-8", "not found".toByteArray())
                }
            }
        } catch (_: Throwable) {
            // Broken pipe / timeout from a disconnected client: nothing to recover.
        }
    }

    private fun handleUpload(client: Socket, input: BufferedInputStream, rawPath: String, contentLength: Long) {
        val encodedName = rawPath.removePrefix(UPLOAD_PATH).trim('/')
        val decoded = runCatching { URLDecoder.decode(encodedName, "UTF-8") }.getOrDefault(encodedName)
        val name = InternalMusicLibrary.sanitizeName(decoded)
        val allowed = InternalMusicLibrary.isAudioFile(name) || name.lowercase(Locale.ROOT).endsWith(".lrc")
        if (!allowed || contentLength <= 0) {
            val msg = if (!allowed) "不支持的文件格式: $name" else "文件内容为空"
            respond(client, 400, "text/plain; charset=utf-8", msg.toByteArray(Charsets.UTF_8))
            return
        }

        val staging = java.io.File.createTempFile("lazer_upload", ".tmp", appContext.cacheDir)
        try {
            staging.outputStream().use { output ->
                var remaining = contentLength
                val buffer = ByteArray(64 * 1024)
                while (remaining > 0) {
                    val chunk = input.read(buffer, 0, buffer.size.coerceAtMost(remaining.toInt()))
                    if (chunk < 0) break
                    output.write(buffer, 0, chunk)
                    remaining -= chunk
                }
            }
            if (remainingBytes(staging, contentLength)) {
                respond(client, 400, "text/plain; charset=utf-8", "上传中断，数据不完整".toByteArray(Charsets.UTF_8))
                return
            }
            val stored = staging.inputStream().use { stream ->
                InternalMusicLibrary.importBytes(appContext, name, stream)
            }
            onImported(stored.name)
            respond(client, 200, "text/plain; charset=utf-8", "已成功导入: ${stored.name}".toByteArray(Charsets.UTF_8))
        } catch (error: Throwable) {
            val err = "导入失败: ${error.message ?: error.javaClass.simpleName}"
            respond(client, 500, "text/plain; charset=utf-8", err.toByteArray(Charsets.UTF_8))
        } finally {
            staging.delete()
        }
    }

    private fun remainingBytes(staging: java.io.File, expected: Long): Boolean = staging.length() != expected

    private fun respond(socket: Socket, code: Int, contentType: String, body: ByteArray) {
        val status = when (code) {
            200 -> "OK"
            204 -> "No Content"
            400 -> "Bad Request"
            404 -> "Not Found"
            500 -> "Internal Server Error"
            else -> "OK"
        }
        val head = "HTTP/1.1 $code $status\r\n" +
            "Content-Type: $contentType\r\n" +
            "Content-Length: ${body.size}\r\n" +
            "Access-Control-Allow-Origin: *\r\n" +
            "Access-Control-Allow-Methods: GET, POST, OPTIONS\r\n" +
            "Access-Control-Allow-Headers: *\r\n" +
            "Connection: close\r\n" +
            "\r\n"
        socket.getOutputStream().use { output ->
            output.write(head.toByteArray(Charsets.UTF_8))
            output.write(body)
            output.flush()
        }
    }

    private fun readLine(input: BufferedInputStream): String? {
        val builder = StringBuilder(96)
        while (true) {
            val byte = input.read()
            if (byte < 0) return if (builder.isEmpty()) null else builder.toString()
            if (byte == '\n'.code) return builder.toString().trimEnd('\r')
            builder.append(byte.toChar())
        }
    }

    companion object {
        const val PORT = 8765
        private const val UPLOAD_PATH = "/upload/"

        /** Best-effort list of LAN IPv4 addresses to show in the UI. */
        fun lanAddresses(): List<String> = runCatching {
            val lanList = mutableListOf<String>()
            val fallbackList = mutableListOf<String>()
            var isEmulatorNat = false

            val interfaces = java.net.NetworkInterface.getNetworkInterfaces() ?: return@runCatching emptyList<String>()
            val ifaceList = interfaces.toList().filter { it.isUp && !it.isLoopback }

            // Sort so physical LAN (wlan / eth / ap / rndis) are prioritized over virtual/cellular
            val sorted = ifaceList.sortedByDescending { nif ->
                val name = nif.name.lowercase(Locale.ROOT)
                when {
                    name.startsWith("wlan") -> 100
                    name.startsWith("ap") -> 90
                    name.startsWith("eth") || name.startsWith("en") -> 80
                    name.startsWith("rndis") -> 70
                    name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("pdp") -> -10
                    name.startsWith("tun") || name.startsWith("tap") || name.startsWith("dummy") || name.startsWith("p2p") -> -20
                    else -> 10
                }
            }

            for (nif in sorted) {
                val name = nif.name.lowercase(Locale.ROOT)
                val isCellularOrVpn = name.startsWith("rmnet") || name.startsWith("ccmni") ||
                    name.startsWith("pdp") || name.startsWith("tun") || name.startsWith("tap") ||
                    name.startsWith("p2p") || name.startsWith("dummy")

                for (addr in nif.inetAddresses) {
                    if (addr.isLoopbackAddress || addr.hostAddress?.contains(':') == true) continue
                    val host = addr.hostAddress ?: continue
                    if (host.startsWith("10.0.2.") || host.startsWith("10.0.3.")) {
                        isEmulatorNat = true
                    }
                    if (!isCellularOrVpn) {
                        lanList.add(host)
                    } else {
                        fallbackList.add(host)
                    }
                }
            }

            val finalResult = (if (lanList.isNotEmpty()) lanList else fallbackList).distinct().toMutableList()
            if (isEmulatorNat || isEmulator()) {
                if (!finalResult.contains("127.0.0.1")) {
                    finalResult.add(0, "127.0.0.1")
                }
            }
            finalResult
        }.getOrDefault(emptyList())

        fun isEmulator(): Boolean {
            val model = Build.MODEL.lowercase(Locale.ROOT)
            val brand = Build.BRAND.lowercase(Locale.ROOT)
            val device = Build.DEVICE.lowercase(Locale.ROOT)
            val finger = Build.FINGERPRINT.lowercase(Locale.ROOT)
            val product = Build.PRODUCT.lowercase(Locale.ROOT)
            val hardware = Build.HARDWARE.lowercase(Locale.ROOT)
            return finger.startsWith("generic") || finger.contains("vbox") ||
                model.contains("google_sdk") || model.contains("emulator") ||
                hardware.contains("goldfish") || hardware.contains("ranchu") ||
                product.contains("vbox") || product.contains("nemu") ||
                brand.startsWith("generic") || device.startsWith("generic")
        }

        private val UPLOAD_PAGE = """
            <!doctype html>
            <html lang="zh">
            <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Music Hub 导入</title>
            <style>
              *{box-sizing:border-box}
              html,body{height:100%}
              body{margin:0;font-family:system-ui,-apple-system,"PingFang SC","Microsoft YaHei",sans-serif;
                   color:#202b36;background:#eef1f6;overflow-x:hidden}
              .stage{position:fixed;inset:0;z-index:-1;overflow:hidden;
                     background:linear-gradient(180deg,#f4f6fa 0%,#ecf0f7 55%,#e9eef5 100%)}
              .orb{position:absolute;border-radius:50%;filter:blur(70px);opacity:.5;
                   animation:drift 22s ease-in-out infinite alternate}
              .orb.pink{width:44vmax;height:44vmax;left:-12vmax;top:-16vmax;background:#f5c7dd}
              .orb.blue{width:40vmax;height:40vmax;right:-10vmax;top:6vmax;background:#a9c8e8;animation-delay:-7s}
              .orb.teal{width:38vmax;height:38vmax;left:16vmax;bottom:-18vmax;background:#a9dfd6;animation-delay:-13s}
              .orb.amber{width:30vmax;height:30vmax;right:8vmax;bottom:-10vmax;background:#ecd9b8;animation-delay:-17s}
              @keyframes drift{from{transform:translate3d(0,0,0) scale(1)}to{transform:translate3d(5vw,6vh,0) scale(1.18)}}
              .veil{position:fixed;inset:0;z-index:-1;
                    background:radial-gradient(120% 90% at 50% 30%,rgba(255,255,255,0) 40%,rgba(236,240,246,.55) 100%)}
              .wrap{max-width:640px;margin:0 auto;padding:48px 20px 60px}
              .card{background:rgba(255,255,255,.58);backdrop-filter:blur(28px) saturate(1.5);
                    -webkit-backdrop-filter:blur(28px) saturate(1.5);
                    border:1px solid rgba(255,255,255,.75);border-radius:30px;
                    box-shadow:0 22px 50px -18px rgba(40,60,90,.28),inset 0 1px 0 rgba(255,255,255,.85);
                    padding:30px 28px 26px}
              .brand{display:flex;align-items:center;gap:12px;margin-bottom:4px}
              .logo{width:40px;height:40px;border-radius:14px;flex:none;
                    background:linear-gradient(135deg,#59b7ab,#4f8fd6);
                    box-shadow:0 6px 16px -6px rgba(79,143,214,.6);
                    display:flex;align-items:center;justify-content:center;color:#fff;font-weight:800;font-size:19px}
              h1{font-size:23px;margin:0;letter-spacing:.2px}
              p.sub{margin:8px 0 0;color:#5c6a7a;font-size:13.5px;line-height:1.55}
              .drop{margin-top:22px;border:1.5px dashed rgba(90,120,160,.45);border-radius:20px;
                    padding:34px 18px;text-align:center;cursor:pointer;background:rgba(255,255,255,.35);
                    transition:border-color .2s,background .2s,transform .15s}
              .drop:hover{background:rgba(255,255,255,.55)}
              .drop.hover{border-color:#4f9ed6;background:rgba(240,248,255,.75);transform:scale(1.012)}
              .drop .big{font-size:15px;font-weight:600}
              .drop .hint{display:block;color:#76838f;font-size:12.5px;margin-top:7px}
              .status-bar{margin-top:16px;font-size:13px;color:#4f8fd6;font-weight:600;display:none}
              ul{list-style:none;padding:0;margin:16px 0 0;display:flex;flex-direction:column;gap:10px}
              li{background:rgba(255,255,255,.65);border:1px solid rgba(255,255,255,.75);border-radius:16px;
                 padding:12px 15px;font-size:13.5px;box-shadow:0 6px 16px -10px rgba(40,60,90,.25);
                 animation:rise .35s cubic-bezier(.2,.8,.3,1.1)}
              @keyframes rise{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:none}}
              .meta{display:flex;justify-content:space-between;gap:10px}
              .name{font-weight:600;overflow:hidden;text-overflow:ellipsis;white-space:nowrap}
              .size{color:#8a95a2;flex:none}
              .state{margin-top:3px;font-size:12px;color:#8a95a2}
              .state.ok{color:#2c8a5e;font-weight:600}
              .state.err{color:#c25549;font-weight:600}
              .bar{height:5px;border-radius:3px;background:rgba(90,120,160,.18);margin-top:8px;overflow:hidden}
              .bar i{display:block;height:100%;width:0;border-radius:3px;
                     background:linear-gradient(90deg,#59b7ab,#4f8fd6);transition:width .15s}
              .foot{margin-top:20px;text-align:center;color:#8a95a2;font-size:12px}
            </style>
            </head>
            <body>
            <div class="stage"><div class="orb pink"></div><div class="orb blue"></div><div class="orb teal"></div><div class="orb amber"></div></div>
            <div class="veil"></div>
            <div class="wrap">
              <div class="card">
                <div class="brand"><div class="logo">M</div><h1>Music Hub 导入</h1></div>
                <p class="sub">选择音频文件上传到应用内部曲库（仅存入应用私有目录，与手机外部音乐互不影响）。同名 .lrc 会作为歌词一并导入。</p>
                <div class="drop" id="drop">
                  <div class="big">点击选择或拖拽文件到这里</div>
                  <span class="hint">支持 mp3 / flac / wav / m4a / ogg / aac / opus / lrc，可多选</span>
                </div>
                <input type="file" id="picker" multiple accept=".mp3,.flac,.wav,.m4a,.ogg,.aac,.opus,.lrc,audio/*" hidden>
                <div class="status-bar" id="statusBar"></div>
                <ul id="list"></ul>
                <div class="foot">仅同一局域网内可访问 · 上传完成后回到应用即可看到</div>
              </div>
            </div>
            <script>
            const drop = document.getElementById('drop');
            const picker = document.getElementById('picker');
            const list = document.getElementById('list');
            const statusBar = document.getElementById('statusBar');

            drop.onclick = () => picker.click();
            drop.ondragover = e => { e.preventDefault(); drop.classList.add('hover'); };
            drop.ondragleave = () => drop.classList.remove('hover');
            drop.ondrop = e => {
              e.preventDefault();
              drop.classList.remove('hover');
              if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
                upload([...e.dataTransfer.files]);
              }
            };
            picker.onchange = () => {
              if (picker.files && picker.files.length > 0) {
                upload([...picker.files]);
                picker.value = '';
              }
            };

            function escapeHtml(s) {
              return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
            }

            const queue = [];
            let active = false;
            let totalCount = 0;
            let completedCount = 0;

            function updateStatus() {
              if (totalCount === 0) {
                statusBar.style.display = 'none';
              } else if (completedCount < totalCount) {
                statusBar.style.display = 'block';
                statusBar.textContent = '正在导入进度: ' + completedCount + ' / ' + totalCount + ' 首…';
              } else {
                statusBar.style.display = 'block';
                statusBar.textContent = '🎉 全部导入完毕！共 ' + totalCount + ' 个文件';
              }
            }

            function upload(files) {
              const valid = [];
              const ignored = [];
              for (const f of files) {
                if (/\.(mp3|flac|wav|m4a|ogg|aac|opus|lrc)$/i.test(f.name)) {
                  valid.push(f);
                } else {
                  ignored.push(f.name);
                }
              }
              if (ignored.length > 0) {
                alert('已跳过非音频/歌词文件 (' + ignored.length + ' 个):\n' + ignored.slice(0, 3).join('\n') + (ignored.length > 3 ? '\n...' : ''));
              }
              totalCount += valid.length;
              updateStatus();

              for (const f of valid) {
                const li = document.createElement('li');
                li.innerHTML = '<div class="meta"><span class="name">' + escapeHtml(f.name) + '</span><span class="size">' + (f.size / 1048576).toFixed(1) + ' MB</span></div><div class="state">排队中…</div><div class="bar"><i></i></div>';
                list.appendChild(li);
                queue.push({ file: f, li: li });
              }
              processQueue();
            }

            function processQueue() {
              if (active || queue.length === 0) return;
              active = true;
              const item = queue.shift();
              const f = item.file;
              const li = item.li;
              const bar = li.querySelector('.bar i');
              const state = li.querySelector('.state');
              state.textContent = '上传中…';

              const xhr = new XMLHttpRequest();
              xhr.open('POST', '/upload/' + encodeURIComponent(f.name));
              xhr.setRequestHeader('Content-Type', 'application/octet-stream');
              xhr.upload.onprogress = e => {
                if (e.lengthComputable) bar.style.width = (e.loaded / e.total * 100) + '%';
              };
              xhr.onload = () => {
                completedCount++;
                updateStatus();
                if (xhr.status === 200) {
                  bar.style.width = '100%';
                  state.textContent = '已导入';
                  state.className = 'state ok';
                } else {
                  state.textContent = '导入失败 (' + xhr.status + ': ' + (xhr.responseText || '错误') + ')';
                  state.className = 'state err';
                }
                active = false;
                processQueue();
              };
              xhr.onerror = () => {
                completedCount++;
                updateStatus();
                state.textContent = '网络连接错误，上传失败';
                state.className = 'state err';
                active = false;
                processQueue();
              };
              xhr.send(f);
            }
            </script>
            </body>
            </html>
        """.trimIndent()
    }
}
