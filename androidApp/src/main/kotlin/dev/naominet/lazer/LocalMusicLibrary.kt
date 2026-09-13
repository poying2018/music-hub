package dev.naominet.lazer

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore

/**
 * Reads the device music index from MediaStore. Every query stays on the caller's dispatcher; the
 * controller decides when to run and surfaces loading or permission states to the UI.
 */
internal object LocalMusicLibrary {

    private val ALBUM_ART_BASE_URI = Uri.parse("content://media/external/audio/albumart")

    fun hasAudioPermission(context: Context): Boolean =
        context.checkSelfPermission(audioPermission()) == PackageManager.PERMISSION_GRANTED

    fun audioPermission(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_AUDIO
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

    suspend fun scan(context: Context): List<AndroidTrack> {
        val tracks = mutableListOf<AndroidTrack>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
        )
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        val selectionArgs = arrayOf(MIN_DURATION_MILLIS.toString())
        context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DEFAULT_SORT_ORDER}",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val albumId = cursor.getLong(albumIdColumn)
                val contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                tracks += AndroidTrack(
                    id = id,
                    title = cursor.getString(titleColumn)?.takeIf(String::isNotBlank)
                        ?: tr("track.unknown_song"),
                    artist = cursor.getString(artistColumn)?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                        ?: tr("track.unknown_artist.android"),
                    album = cursor.getString(albumColumn)?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }
                        .orEmpty(),
                    durationMillis = cursor.getLong(durationColumn),
                    contentUri = contentUri.toString(),
                    coverUrl = ContentUris.withAppendedId(ALBUM_ART_BASE_URI, albumId).toString(),
                    dataPath = cursor.getString(dataColumn),
                )
            }
        }
        return tracks.distinctBy(AndroidTrack::contentUri)
    }

    private const val MIN_DURATION_MILLIS = 30_000L
}
