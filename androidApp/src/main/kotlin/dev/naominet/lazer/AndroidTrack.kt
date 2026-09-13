package dev.naominet.lazer

/**
 * A single playable song on the device, resolved from MediaStore. The content URI is what the
 * playback service hands to MediaPlayer; the album art URI is read directly by Coil.
 */
data class AndroidTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMillis: Long,
    val contentUri: String,
    val coverUrl: String? = null,
    val dataPath: String? = null,
) {
    val durationLabel: String get() = formatPlaybackTime(durationMillis)

    /** Artist line for display; internal-library files may carry a blank artist. */
    val displayArtist: String get() = artist.ifBlank { tr("track.unknown_artist.android") }
}

/** Preferred way to locate a same-named .lrc file next to the audio file. */
fun lrcPathForTrack(track: AndroidTrack): String? = track.dataPath
    ?.substringBeforeLast('.')
    ?.takeIf(String::isNotBlank)
    ?.plus(".lrc")

fun formatPlaybackTime(millis: Long): String {
    val seconds = (millis.coerceAtLeast(0L) / 1_000L).toInt()
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
