package dev.naominet.lazer.core

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import dev.naominet.lazer.AndroidTrack
import dev.naominet.lazer.tr
import java.io.File
import java.io.InputStream

/**
 * The app-internal music library, stored in the app's private directory and completely isolated
 * from the device MediaStore. Tracks enter only through explicit import: WiFi upload, WebDAV
 * download, or the system file picker / share sheet. A same-named `.lrc` file next to the audio
 * file is picked up by the shared lyrics logic via [AndroidTrack.dataPath].
 */
internal object InternalMusicLibrary {

    private val AUDIO_EXTENSIONS = setOf("mp3", "flac", "wav", "m4a", "ogg", "aac", "opus")

    fun musicDir(context: Context): File = File(context.filesDir, "InternalMusic").apply { mkdirs() }
    private fun artDir(context: Context): File = File(context.filesDir, "InternalMusicArt").apply { mkdirs() }

    /** Stable id derived from the file name, so favorites/recents survive restarts. */
    fun stableId(fileName: String): Long = fileName.hashCode().toLong() and 0x7FFFFFFFFFFFFFFFL

    fun sanitizeName(raw: String): String {
        val base = raw.substringAfterLast('/').substringAfterLast('\\').trim().ifEmpty { "import.mp3" }
        return base.replace(Regex("[\\\\/:*?\"<>|\u0000]"), "_")
    }

    private fun extensionOf(name: String): String = name.substringAfterLast('.', "").lowercase()

    fun isAudioFile(name: String): Boolean = extensionOf(name) in AUDIO_EXTENSIONS

    /** Lists the internal library; never touches MediaStore. */
    fun scan(context: Context): List<AndroidTrack> {
        val dir = musicDir(context)
        val art = artDir(context)
        return dir.listFiles()
            .orEmpty()
            .filter { it.isFile && isAudioFile(it.name) }
            .map { file ->
                val name = file.name
                val artFile = File(art, "$name.png")
                // No tr() here: scan may finish before translations load, and a key literal
                // would be frozen into the track. Display layers fall back via displayArtist.
                AndroidTrack(
                    id = stableId(name),
                    title = name.substringBeforeLast('.'),
                    artist = "",
                    album = "",
                    durationMillis = probeDuration(context, file),
                    contentUri = Uri.fromFile(file).toString(),
                    coverUrl = if (artFile.isFile) Uri.fromFile(artFile).toString() else null,
                    dataPath = file.absolutePath,
                )
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::title))
    }

    /** Copies an opened stream into the library under [fileName]; returns the stored file. */
    @Throws(java.io.IOException::class)
    fun importBytes(context: Context, fileName: String, input: InputStream): File {
        val safeName = sanitizeName(fileName)
        val ext = extensionOf(safeName)
        // .lrc files are stored as lyric sidecars next to the audio, never listed as tracks.
        require(ext in AUDIO_EXTENSIONS || ext == "lrc") { "unsupported audio type: $safeName" }
        val dir = musicDir(context)
        val target = if (ext == "lrc") File(dir, safeName) else uniqueTarget(dir, safeName)
        input.use { source ->
            target.outputStream().use { output -> source.copyTo(output) }
        }
        if (ext != "lrc") exportEmbeddedArt(context, target)
        return target
    }

    /** Imports from a content:// / file:// uri handed over by SAF or a share sheet. */
    fun importFromUri(context: Context, uri: Uri): File? = runCatching {
        val name = queryDisplayName(context, uri) ?: "import.${uri.lastPathSegment ?: "mp3"}"
        context.contentResolver.openInputStream(uri)?.use { input ->
            importBytes(context, name, input)
        }
    }.getOrNull()

    fun delete(context: Context, track: AndroidTrack): Boolean {
        val path = track.dataPath ?: return false
        val file = File(path)
        val art = File(artDir(context), file.name + ".png")
        return file.delete() and (if (art.isFile) art.delete() else true)
    }

    /** Writes a sidecar PNG of the embedded cover so Coil and the notification can reuse it. */
    private fun exportEmbeddedArt(context: Context, audioFile: File) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(audioFile.absolutePath)
            val bytes = retriever.embeddedPicture ?: return
            val art = File(artDir(context), audioFile.name + ".png")
            art.outputStream().use { output -> output.write(bytes) }
        } catch (_: Throwable) {
            // No embedded art or unreadable file: the gradient placeholder covers this case.
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun probeDuration(context: Context, file: File): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        } catch (_: Throwable) {
            0L
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun uniqueTarget(dir: File, fileName: String): File {
        var candidate = File(dir, fileName)
        if (!candidate.exists()) return candidate
        val base = fileName.substringBeforeLast('.')
        val ext = fileName.substringAfterLast('.', "")
        var index = 2
        while (candidate.exists()) {
            val name = if (ext.isEmpty()) "$base ($index)" else "$base ($index).$ext"
            candidate = File(dir, name)
            index++
        }
        return candidate
    }

    private fun queryDisplayName(context: Context, uri: Uri): String? = runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && cursor.moveToFirst()) cursor.getString(idx) else null
        }
    }.getOrNull()
}
