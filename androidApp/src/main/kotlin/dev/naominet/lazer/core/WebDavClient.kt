package dev.naominet.lazer.core

import android.content.Context
import android.content.SharedPreferences
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import android.util.Base64

/** One entry of a WebDAV collection: a folder or a file. */
data class WebDavEntry(
    val name: String,
    val isFolder: Boolean,
    val sizeBytes: Long,
)

/** Small WebDAV reader (PROPFIND + GET with Basic auth), enough for 坚果云/群晖/Alist bridged drives. */
internal class WebDavClient(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString(KEY_URL, "")?.trimEnd('/') ?: ""
        set(value) = prefs.edit().putString(KEY_URL, value.trim().trimEnd('/')).apply()

    var username: String
        get() = prefs.getString(KEY_USER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER, value).apply()

    var password: String
        get() = prefs.getString(KEY_PASS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_PASS, value).apply()

    /** Browse path relative to [baseUrl]; "" is the root. Folders keep their trailing slash. */
    fun list(path: String): List<WebDavEntry> {
        val target = resolve(path)
        val connection = open(target, method = "PROPFIND")
        try {
            connection.setRequestProperty("Depth", "1")
            connection.setRequestProperty("Content-Type", "application/xml")
            connection.doOutput = true
            connection.outputStream.use { it.write(PROPFIND_BODY.toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            if (code !in 200..299) throw java.io.IOException("WebDAV $code")
            val body = connection.inputStream.readBytes().toString(Charsets.UTF_8)
            return parseListing(body, path)
        } finally {
            connection.disconnect()
        }
    }

    /** Opens the file at [path] for streaming into the internal library. */
    fun openDownload(path: String): InputStream {
        val connection = open(resolve(path), method = "GET")
        if (connection.responseCode !in 200..299) {
            connection.disconnect()
            throw java.io.IOException("WebDAV ${connection.responseCode}")
        }
        return connection.inputStream
    }

    fun disconnect(stream: InputStream) {
        runCatching { stream.close() }
    }

    private fun resolve(path: String): String {
        val clean = path.trim('/')
        val encoded = clean.split('/').filter { it.isNotEmpty() }.joinToString("/") { segment ->
            java.net.URLEncoder.encode(segment, "UTF-8").replace("+", "%20")
        }
        return if (encoded.isEmpty()) baseUrl else "$baseUrl/$encoded"
    }

    private fun open(url: String, method: String): HttpURLConnection {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 20_000
        connection.requestMethod = method
        if (username.isNotEmpty() || password.isNotEmpty()) {
            val token = Base64.encodeToString("$username:$password".toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            connection.setRequestProperty("Authorization", "Basic $token")
        }
        return connection
    }

    /** Minimal href extraction; WebDAV servers emit `<D:response><D:href>` (any case/prefix). */
    private fun parseListing(body: String, currentPath: String): List<WebDavEntry> {
        val selfHref = normalize(currentPath)
        val entries = mutableListOf<WebDavEntry>()
        val responseRegex = Regex("<(?:[\\w-]*:)?response[^>]*>(.*?)</(?:[\\w-]*:)?response>", RegexOption.DOT_MATCHES_ALL)
        val hrefRegex = Regex("<(?:[\\w-]*:)?href[^>]*>(.*?)</(?:[\\w-]*:)?href>", RegexOption.DOT_MATCHES_ALL)
        val collectionRegex = Regex("<(?:[\\w-]*:)?collection\\s*/?>", RegexOption.IGNORE_CASE)
        val sizeRegex = Regex("<(?:[\\w-]*:)?getcontentlength[^>]*>(\\d+)</(?:[\\w-]*:)?getcontentlength>")
        for (match in responseRegex.findAll(body)) {
            val block = match.groupValues[1]
            val href = hrefRegex.find(block)?.groupValues?.get(1)?.trim() ?: continue
            val decoded = decodeHref(href)
            if (normalize(decoded) == selfHref) continue // the requested folder itself
            val name = decoded.trimEnd('/').substringAfterLast('/')
            if (name.isEmpty()) continue
            val isFolder = collectionRegex.containsMatchIn(block) || decoded.endsWith("/")
            val size = sizeRegex.find(block)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            entries += WebDavEntry(name, isFolder, size)
        }
        return entries.sortedWith(compareByDescending<WebDavEntry> { it.isFolder }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.name })
    }

    private fun decodeHref(href: String): String = runCatching { URLDecoder.decode(href, "UTF-8") }.getOrDefault(href)

    private fun normalize(path: String): String = path.trim('/').ifEmpty { "/" }

    private companion object {
        const val PREFS = "lazer.webdav"
        const val KEY_URL = "url"
        const val KEY_USER = "user"
        const val KEY_PASS = "pass"

        val PROPFIND_BODY = """
            <?xml version="1.0" encoding="utf-8"?>
            <D:propfind xmlns:D="DAV:">
              <D:prop>
                <D:displayname/>
                <D:resourcetype/>
                <D:getcontentlength/>
              </D:prop>
            </D:propfind>
        """.trimIndent()
    }
}
