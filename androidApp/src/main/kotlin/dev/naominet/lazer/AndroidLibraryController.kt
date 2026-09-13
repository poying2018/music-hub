package dev.naominet.lazer

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.asImageBitmap
import dev.naominet.lazer.core.InternalMusicLibrary
import dev.naominet.lazer.core.WebDavClient
import dev.naominet.lazer.core.WebDavEntry
import dev.naominet.lazer.core.WifiTransferServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** How the library list orders its tracks. */
enum class AndroidLibrarySort(val labelKey: String) {
    TITLE("library.sort.title"),
    ARTIST("library.sort.artist"),
    ALBUM("library.sort.album");

    val label: String get() = tr(labelKey)
}

/** Which subset of the local library the library page shows. */
enum class AndroidLibraryFilter(val labelKey: String) {
    ALL("library.all"),
    FAVORITES("library.favorites"),
    RECENT("library.recent");

    val label: String get() = tr(labelKey)
}

/**
 * Android presentation state for the local music player. The device MediaStore is the only music
 * source: scanning, favorites, recents, and .lrc lyrics all resolve locally.
 */
class AndroidLibraryController(context: Context) {
    private val appContext = context.applicationContext
    private val settings = AndroidSettingsStore(appContext)
    private val libraryStore = appContext.getSharedPreferences("lazer.android.library", Context.MODE_PRIVATE)
    private val webDavClient = WebDavClient(appContext)
    private var wifiServer: WifiTransferServer? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var deviceScanJob: Job? = null
    private var internalScanJob: Job? = null
    private var lyricJob: Job? = null

    var destination by mutableStateOf(AndroidRootDestination.LIBRARY)
        private set
    var skin by mutableStateOf(settings.skin)
        private set
    var glassThemeMode by mutableStateOf(settings.glassThemeMode)
        private set
    var isDark by mutableStateOf(settings.isDark)
        private set
    var useSystemMonetColors by mutableStateOf(settings.useSystemMonetColors)
        private set
    var palette by mutableStateOf(settings.palette)
        private set
    var backgroundImage by mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null)
        private set
    var backgroundImageEnabled by mutableStateOf(settings.backgroundImageEnabled)
        private set
    var backgroundAlpha by mutableStateOf(settings.backgroundAlpha)
        private set
    var style by mutableStateOf(settings.style)
        private set
    val themeEngine: LazerThemeEngine get() = style.themeEngine
    val liquidGlassEnabled: Boolean get() = style.usesLiquidGlass
    var language by mutableStateOf(settings.language)
        private set
    var lyricFollowDelayMillis by mutableStateOf(settings.lyricFollowDelayMillis)
        private set
    var lyricAnimationSpeed by mutableStateOf(settings.lyricAnimationSpeed)
        private set
    var wordLyricsEnabled by mutableStateOf(settings.wordLyricsEnabled)
        private set
    var lyricGlowEnabled by mutableStateOf(settings.lyricGlowEnabled)
        private set
    var lyricFontSizeSp by mutableIntStateOf(settings.lyricFontSizeSp)
        private set
    var showFullLyrics by mutableStateOf(settings.showFullLyrics)
        private set
    var message by mutableStateOf<String?>(null)
        private set

    var tracks by mutableStateOf<List<AndroidTrack>>(emptyList())
        private set
    var isScanning by mutableStateOf(false)
        private set
    var musicSource by mutableStateOf(settings.musicSource)
        private set
    var internalTracks by mutableStateOf<List<AndroidTrack>>(emptyList())
        private set
    var isInternalScanning by mutableStateOf(false)
        private set
    var wifiServerRunning by mutableStateOf(false)
        private set
    var wifiAddresses by mutableStateOf<List<String>>(emptyList())
        private set
    var webdavEntries by mutableStateOf<List<WebDavEntry>>(emptyList())
        private set
    var webdavPath by mutableStateOf("")
        private set
    var webdavLoading by mutableStateOf(false)
        private set
    var webdavDownloading by mutableStateOf<String?>(null)
        private set
    var libraryFilter by mutableStateOf(AndroidLibraryFilter.ALL)
        private set
    var librarySort by mutableStateOf(AndroidLibrarySort.TITLE)
        private set
    var favoriteIds by mutableStateOf(emptySet<Long>())
        private set
    var recentTrackIds by mutableStateOf(emptyList<Long>())
        private set
    var searchQuery by mutableStateOf("")
        private set

    var lyrics by mutableStateOf<List<AndroidTimedLyricLine>>(emptyList())
        private set
    var lyricsLoading by mutableStateOf(false)
        private set
    var lyricsMessage by mutableStateOf<String?>(null)
        private set

    init {
        LazerI18n.switchLanguage(language)
        favoriteIds = libraryStore.getStringSet(KEY_FAVORITES, emptySet())
            .orEmpty()
            .mapNotNull(String::toLongOrNull)
            .toSet()
        recentTrackIds = runCatching {
            val raw = libraryStore.getString(KEY_RECENT, null) ?: return@runCatching emptyList()
            raw.split(',').mapNotNull(String::toLongOrNull)
        }.getOrDefault(emptyList())
        scope.launch {
            loadLazerTranslations()
            loadBackgroundImage()
            refreshInternalLibrary()
        }
    }

    fun hasAudioPermission(): Boolean = LocalMusicLibrary.hasAudioPermission(appContext)

    /** Requests nothing itself; the UI owns the permission launcher and reports the outcome here. */
    fun refreshLibrary() {
        if (!hasAudioPermission()) {
            message = tr("status.play_permission")
            return
        }
        deviceScanJob?.cancel()
        deviceScanJob = scope.launch {
            isScanning = true
            message = null
            try {
                val scanned = withContext(Dispatchers.IO) { LocalMusicLibrary.scan(appContext) }
                tracks = scanned
                if (scanned.isEmpty()) message = null
            } catch (error: Throwable) {
                android.util.Log.e(TAG, "library scan failed", error)
                message = tr("status.scan_fail")
            } finally {
                isScanning = false
            }
        }
    }

    fun selectDestination(value: AndroidRootDestination) {
        destination = value
        message = null
    }

    fun updateSkin(value: AppSkin) {
        if (skin == value) return
        skin = value
        settings.skin = value
    }

    fun updateGlassThemeMode(value: GlassThemeMode) {
        if (glassThemeMode == value) return
        glassThemeMode = value
        settings.glassThemeMode = value
    }

    fun toggleTheme() {
        isDark = !isDark
        settings.isDark = isDark
    }

    fun updateUseSystemMonetColors(enabled: Boolean) {
        useSystemMonetColors = enabled
        settings.useSystemMonetColors = enabled
    }

    fun updatePalette(value: LazerPalette) {
        palette = value
        settings.palette = value
    }

    fun updateBackgroundAlpha(value: Float) {
        backgroundAlpha = value.coerceIn(0f, 1f)
        settings.backgroundAlpha = backgroundAlpha
    }

    fun updateBackgroundImageEnabled(enabled: Boolean) {
        backgroundImageEnabled = enabled
        settings.backgroundImageEnabled = enabled
    }

    fun updateStyle(value: LazerStyle) {
        style = value
        settings.style = value
    }

    fun updateLanguage(value: LazerLanguage) {
        if (language == value) return
        language = value
        LazerI18n.switchLanguage(value)
        settings.language = value
        message = null
    }

    fun updateLyricFollowDelay(value: Long) {
        lyricFollowDelayMillis = normalizeLyricFollowDelayMillis(value)
        settings.lyricFollowDelayMillis = lyricFollowDelayMillis
    }

    fun updateLyricAnimationSpeed(value: LyricAnimationSpeed) {
        lyricAnimationSpeed = value
        settings.lyricAnimationSpeed = value
    }

    fun updateWordLyricsEnabled(enabled: Boolean) {
        wordLyricsEnabled = enabled
        settings.wordLyricsEnabled = enabled
    }

    fun updateLyricGlowEnabled(enabled: Boolean) {
        lyricGlowEnabled = enabled
        settings.lyricGlowEnabled = enabled
    }

    fun updateLyricFontSizeSp(value: Int) {
        lyricFontSizeSp = normalizeLyricFontSizeSp(value)
        settings.lyricFontSizeSp = lyricFontSizeSp
    }

    fun updateShowFullLyrics(enabled: Boolean) {
        showFullLyrics = enabled
        settings.showFullLyrics = enabled
    }

    fun updateLibraryFilter(value: AndroidLibraryFilter) {
        libraryFilter = value
    }

    fun updateLibrarySort(value: AndroidLibrarySort) {
        librarySort = value
    }

    // ---- Internal library (app-private, fully isolated from MediaStore) ----

    fun updateMusicSource(value: MusicSource) {
        if (musicSource == value) return
        musicSource = value
        settings.musicSource = value
        if (value == MusicSource.INTERNAL) refreshInternalLibrary()
        stopWifiServer()
    }

    fun refreshInternalLibrary() {
        internalScanJob?.cancel()
        internalScanJob = scope.launch {
            isInternalScanning = true
            try {
                internalTracks = withContext(Dispatchers.IO) { InternalMusicLibrary.scan(appContext) }
            } catch (error: Throwable) {
                android.util.Log.e(TAG, "internal library scan failed", error)
            } finally {
                isInternalScanning = false
            }
        }
    }

    fun importFromUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        scope.launch {
            val imported = withContext(Dispatchers.IO) {
                uris.count { uri -> InternalMusicLibrary.importFromUri(appContext, uri) != null }
            }
            refreshInternalLibrary()
            message = if (imported > 0) tr("status.import_done", imported) else tr("status.import_fail")
        }
    }

    fun deleteInternalTrack(track: AndroidTrack) {
        scope.launch {
            val removed = withContext(Dispatchers.IO) { InternalMusicLibrary.delete(appContext, track) }
            if (removed) refreshInternalLibrary()
        }
    }

    fun startWifiServer() {
        if (wifiServerRunning) return
        scope.launch {
            try {
                val server = WifiTransferServer(appContext, onImported = {
                    scope.launch { refreshInternalLibrary() }
                })
                withContext(Dispatchers.IO) { server.start() }
                wifiServer = server
                wifiAddresses = withContext(Dispatchers.IO) { WifiTransferServer.lanAddresses() }
                wifiServerRunning = true
            } catch (error: Throwable) {
                android.util.Log.e(TAG, "wifi server start failed", error)
                message = tr("wifi.fail")
            }
        }
    }

    fun stopWifiServer() {
        val server = wifiServer
        wifiServer = null
        scope.launch(Dispatchers.IO) {
            server?.let { runCatching { it.stop() } }
        }
        if (wifiServerRunning) {
            wifiServerRunning = false
            wifiAddresses = emptyList()
        }
    }

    // ---- WebDAV import ----

    val webDavSavedUrl: String get() = webDavClient.baseUrl
    val webDavSavedUser: String get() = webDavClient.username
    val webDavSavedPass: String get() = webDavClient.password

    fun connectWebDav(url: String, user: String, pass: String, startPath: String) {
        webDavClient.baseUrl = url
        webDavClient.username = user
        webDavClient.password = pass
        browseWebDav(startPath)
    }

    fun browseWebDav(path: String) {
        if (webdavLoading) return
        webdavLoading = true
        scope.launch {
            try {
                val entries = withContext(Dispatchers.IO) { webDavClient.list(path) }
                webdavEntries = entries
                webdavPath = path.trim('/')
                if (entries.none { !it.isFolder }) message = tr("webdav.empty")
            } catch (_: Throwable) {
                message = tr("webdav.fail")
            } finally {
                webdavLoading = false
            }
        }
    }

    fun downloadWebDav(entry: WebDavEntry) {
        if (webdavDownloading != null) return
        webdavDownloading = entry.name
        scope.launch {
            try {
                withContext(Dispatchers.IO) {
                    webDavClient.openDownload(joinWebDavPath(webdavPath, entry.name)).use { input ->
                        InternalMusicLibrary.importBytes(appContext, entry.name, input)
                    }
                }
                refreshInternalLibrary()
                message = tr("status.import_done", 1)
            } catch (_: Throwable) {
                message = tr("status.import_fail")
            } finally {
                webdavDownloading = null
            }
        }
    }

    private fun joinWebDavPath(dir: String, name: String): String =
        if (dir.isEmpty()) name else "$dir/$name"


    fun updateSearchQuery(value: String) {
        searchQuery = value
    }

    /** The collection behind the library page, per the selected source. */
    private val activeTracks: List<AndroidTrack>
        get() = internalTracks

    /** Search stays in memory: the local library is already fully on the device. */
    val searchResults: List<AndroidTrack>
        get() {
            val query = searchQuery.trim()
            if (query.isBlank()) return emptyList()
            return activeTracks.filter { track ->
                track.title.contains(query, ignoreCase = true) ||
                    track.artist.contains(query, ignoreCase = true) ||
                    track.album.contains(query, ignoreCase = true)
            }
        }

    val displayedTracks: List<AndroidTrack>
        get() {
            // Recency order is the point of that filter; never re-sort it.
            if (libraryFilter == AndroidLibraryFilter.RECENT) {
                return recentTrackIds.mapNotNull { id -> activeTracks.firstOrNull { it.id == id } }
            }
            val base = when (libraryFilter) {
                AndroidLibraryFilter.ALL -> activeTracks
                AndroidLibraryFilter.FAVORITES -> activeTracks.filter { it.id in favoriteIds }
                AndroidLibraryFilter.RECENT -> emptyList()
            }
            return when (librarySort) {
                AndroidLibrarySort.TITLE -> base.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::title),
                )
                AndroidLibrarySort.ARTIST -> base.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::artist)
                        .thenBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::title),
                )
                AndroidLibrarySort.ALBUM -> base.sortedWith(
                    compareBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::album)
                        .thenBy(String.CASE_INSENSITIVE_ORDER, AndroidTrack::title),
                )
            }
        }

    fun isSongLiked(trackId: Long): Boolean = trackId in favoriteIds

    fun toggleSongLiked(track: AndroidTrack) {
        val wasLiked = track.id in favoriteIds
        favoriteIds = if (wasLiked) favoriteIds - track.id else favoriteIds + track.id
        libraryStore.edit().putStringSet(KEY_FAVORITES, favoriteIds.map(Long::toString).toSet()).apply()
    }

    /** The playback snapshot changed to [track]; remember it as the most recently played. */
    fun onPlaybackTrackChanged(track: AndroidTrack) {
        val next = listOf(track.id) + recentTrackIds.filterNot { it == track.id }
        recentTrackIds = next.take(RECENT_LIMIT)
        libraryStore.edit().putString(KEY_RECENT, recentTrackIds.joinToString(",")).apply()
    }

    fun loadLyrics(track: AndroidTrack) {
        lyricJob?.cancel()
        lyrics = emptyList()
        SuperLyricPublisher.updateLyrics(track.id, emptyList())
        lyricsMessage = null
        val path = lrcPathForTrack(track)
        if (path == null) {
            lyricsMessage = tr("status.no_lyrics")
            return
        }
        lyricsLoading = true
        lyricJob = scope.launch {
            val text = withContext(Dispatchers.IO) {
                runCatching { java.io.File(path).readText(Charsets.UTF_8) }.getOrNull()
                    ?: runCatching { java.io.File(path).readText(Charsets.ISO_8859_1) }.getOrNull()
            }
            lyricsLoading = false
            if (text.isNullOrBlank()) {
                lyricsMessage = tr("status.no_lyrics")
                return@launch
            }
            // A second LRC block with matching timestamps commonly carries the translation.
            val parsed = parseAndroidLrc(text)
            lyrics = parsed
            SuperLyricPublisher.updateLyrics(track.id, parsed)
            lyricsMessage = if (parsed.isEmpty()) tr("status.no_lyrics") else null
        }
    }

    fun close() {
        stopWifiServer()
        deviceScanJob?.cancel()
        internalScanJob?.cancel()
        lyricJob?.cancel()
        scope.cancel()
    }

    private suspend fun loadBackgroundImage() {
        val path = settings.backgroundImagePath ?: return
        val bitmap = withContext(Dispatchers.IO) {
            runCatching {
                android.graphics.BitmapFactory.decodeFile(path)?.asImageBitmap()
            }.getOrNull()
        }
        backgroundImage = bitmap
    }

    /** Copies the picked image into app storage and decodes it as the new background. */
    fun setBackgroundImage(uri: android.net.Uri) {
        scope.launch {
            val decoded = withContext(Dispatchers.IO) {
                runCatching {
                    val target = java.io.File(appContext.filesDir, BACKGROUND_IMAGE_FILE)
                    appContext.contentResolver.openInputStream(uri)?.use { input ->
                        target.outputStream().use(input::copyTo)
                    }
                    android.graphics.BitmapFactory.decodeFile(target.absolutePath)?.asImageBitmap()
                }.onFailure { android.util.Log.w(TAG, "background load failed", it) }
                    .getOrNull()
            }
            if (decoded != null) {
                backgroundImage = decoded
                settings.backgroundImagePath = java.io.File(appContext.filesDir, BACKGROUND_IMAGE_FILE).absolutePath
            }
        }
    }

    fun clearBackgroundImage() {
        backgroundImage = null
        settings.backgroundImagePath = null
        runCatching { java.io.File(appContext.filesDir, BACKGROUND_IMAGE_FILE).delete() }
    }

    private companion object {
        const val TAG = "AndroidLibraryController"
        const val BACKGROUND_IMAGE_FILE = "lazer.background.png"
        const val KEY_FAVORITES = "library.favorite_ids"
        const val KEY_RECENT = "library.recent_ids"
        const val RECENT_LIMIT = 50
    }
}

enum class AndroidRootDestination(private val labelKey: String, val motionIndex: Int) {
    LIBRARY("nav.library", 0),
    SEARCH("nav.search", 1),
    SETTINGS("nav.settings", 2);

    val label: String get() = tr(labelKey)
}
