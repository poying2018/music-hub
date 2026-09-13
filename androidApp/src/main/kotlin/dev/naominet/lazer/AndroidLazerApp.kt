package dev.naominet.lazer

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Lyrics
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SortByAlpha
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.core.view.WindowCompat
import coil3.compose.AsyncImage
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop as KyantLayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as kyantLayerBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop as kyantRememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop as kyantRememberLayerBackdrop
import dev.naominet.lazer.core.GlassStyleStore
import dev.naominet.lazer.core.InternalMusicLibrary
import dev.naominet.lazer.core.WifiTransferServer
import dev.naominet.lazer.ui.glass.GlassBackground
import dev.naominet.lazer.ui.glass.LiquidBottomTab
import dev.naominet.lazer.ui.glass.LiquidBottomTabs
import dev.naominet.lazer.ui.glass.LiquidButton
import dev.naominet.lazer.ui.glass.LiquidGlassSegmentedControl
import dev.naominet.lazer.ui.glass.LiquidSlider
import dev.naominet.lazer.ui.glass.glassCard
import dev.naominet.lazer.ui.glass.glassRow
import dev.naominet.lazer.ui.glass.glassTile
import dev.naominet.lazer.ui.glass.rememberPageBackdrop
import dev.naominet.lazer.ui.theme.GlassTheme
import dev.naominet.lazer.ui.theme.LocalGlassPalette
import com.kashif_e.backdrop.backdrops.layerBackdrop
import com.kashif_e.backdrop.backdrops.rememberCombinedBackdrop
import com.kashif_e.backdrop.backdrops.rememberLayerBackdrop
import com.kashif_e.backdrop.drawBackdrop
import com.kashif_e.backdrop.effects.blur
import com.kashif_e.backdrop.effects.lens
import com.kashif_e.backdrop.effects.vibrancy
import com.kashif_e.backdrop.highlight.Highlight
import com.kashif_e.backdrop.shadow.InnerShadow
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
import top.yukonga.miuix.kmp.basic.ButtonColors as MiuixButtonColors
import top.yukonga.miuix.kmp.basic.ButtonDefaults as MiuixButtonDefaults
import top.yukonga.miuix.kmp.basic.Card as MiuixCard
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarDisplayMode as MiuixNavigationBarDisplayMode
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import kotlin.math.roundToLong
import kotlin.math.roundToInt

private const val PAGE_TRANSITION_MILLIS = LazerTokens.Motion.pageMillis
private val LazerMotionEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

// Extra bottom content padding for scrollable pages so their last rows stay reachable behind the
// floating liquid-glass bottom controls.
private val LocalAndroidContentBottomInset = compositionLocalOf { 0.dp }

private enum class AndroidBackLayer {
    PLAYER,
    LYRICS,
}

/** Backdrop layers of the glass stage; only provided under the glass skin. */
private class GlassEnv(val page: KyantLayerBackdrop, val content: KyantLayerBackdrop, val scrim: Backdrop)
private val LocalGlassEnv = compositionLocalOf<GlassEnv?> { null }

private fun Modifier.predictiveBackTransform(
    enabled: Boolean,
    progress: Float,
    swipeEdge: Int,
): Modifier = if (!enabled) {
    this
} else {
    graphicsLayer {
        val fraction = progress.coerceIn(0f, 1f)
        val direction = if (swipeEdge == BackEventCompat.EDGE_RIGHT) -1f else 1f
        translationX = size.width * 0.08f * fraction * direction
        val scale = 1f - 0.035f * fraction
        scaleX = scale
        scaleY = scale
        alpha = 1f - 0.08f * fraction
    }
}

@Composable
private fun isLandscapeLayout(): Boolean =
    LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE

@Composable
private fun ThemeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    cornerRadius: Dp? = null,
    content: @Composable RowScope.() -> Unit,
) {
    if (LocalLazerThemeEngine.current == LazerThemeEngine.MIUIX) {
        val colors = MiuixButtonDefaults.buttonColorsPrimary()
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (enabled) colors.contentColor else colors.disabledContentColor,
        ) {
            MiuixButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                cornerRadius = cornerRadius ?: MiuixButtonDefaults.CornerRadius,
                colors = colors,
                content = content,
            )
        }
    } else if (cornerRadius != null) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            shape = RoundedCornerShape(cornerRadius),
            content = content,
        )
    } else {
        Button(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@Composable
private fun ThemeTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    if (LocalLazerThemeEngine.current == LazerThemeEngine.MIUIX) {
        val textColors = MiuixButtonDefaults.textButtonColors()
        val colors = MiuixButtonColors(
            color = textColors.color,
            disabledColor = textColors.disabledColor,
            contentColor = textColors.textColor,
            disabledContentColor = textColors.disabledTextColor,
        )
        CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (enabled) colors.contentColor else colors.disabledContentColor,
        ) {
            MiuixButton(
                onClick = onClick,
                modifier = modifier,
                enabled = enabled,
                colors = colors,
                content = content,
            )
        }
    } else {
        TextButton(onClick = onClick, modifier = modifier, enabled = enabled, content = content)
    }
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val uiAlpha = LocalLazerUiAlpha.current
    val glassEnv = LocalGlassEnv.current
    // Settings cards live inside a scrolling LazyColumn; drawBackdrop glass draws its rim
    // relative to stale scroll positions there (borders and touch targets drift apart),
    // so the glass skin renders them as solid iOS-style cards instead.
    if (glassEnv != null) {
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides colors.onSurface) {
            Surface(
                modifier = modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)),
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                shadowElevation = 1.dp,
            ) {
                content()
            }
        }
    } else if (LocalLazerThemeEngine.current == LazerThemeEngine.MIUIX) {
        CompositionLocalProvider(androidx.compose.material3.LocalContentColor provides colors.onSurface) {
            MiuixCard(
                modifier = modifier.fillMaxWidth(),
                cornerRadius = 18.dp,
            ) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = colors.surfaceContainerHigh.copy(alpha = uiAlpha),
        ) {
            content()
        }
    }
}

@Composable
private fun ExperimentalBadge() {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = colors.secondaryContainer,
        contentColor = colors.onSecondaryContainer,
    ) {
        Text(
            tr("settings.glass.experimental"),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun AndroidLazerApp() {
    val context = LocalContext.current
    val controller = remember(context.applicationContext) { AndroidLibraryController(context.applicationContext) }
    val playback by AndroidPlaybackConnection.snapshot.collectAsState()
    var playerVisible by remember { mutableStateOf(false) }
    var lyricsVisible by remember { mutableStateOf(false) }
    var requestedBackProgress by remember { mutableFloatStateOf(0f) }
    var isPredictiveBackRunning by remember { mutableStateOf(false) }
    var backSwipeEdge by remember { mutableStateOf(BackEventCompat.EDGE_LEFT) }
    var transformedBackLayer by remember { mutableStateOf<AndroidBackLayer?>(null) }

    val activeBackLayer = when {
        lyricsVisible -> AndroidBackLayer.LYRICS
        playerVisible -> AndroidBackLayer.PLAYER
        else -> null
    }
    val renderedBackProgress by animateFloatAsState(
        targetValue = requestedBackProgress,
        animationSpec = if (isPredictiveBackRunning) {
            snap()
        } else {
            tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing)
        },
        label = "predictive-back-progress",
    )
    LaunchedEffect(isPredictiveBackRunning, renderedBackProgress) {
        if (!isPredictiveBackRunning && renderedBackProgress == 0f) transformedBackLayer = null
    }

    DisposableEffect(controller) {
        onDispose { controller.close() }
    }
    LaunchedEffect(playback.track?.id) {
        playback.track?.let { track ->
            controller.onPlaybackTrackChanged(track)
            controller.loadLyrics(track)
        }
    }

    // The currently visible top layer owns back. Gesture progress drives the same page that a
    // normal back press closes; cancelling the gesture eases that page back into place.
    PredictiveBackHandler(enabled = activeBackLayer != null) { events ->
        val layer = activeBackLayer ?: return@PredictiveBackHandler
        transformedBackLayer = layer
        isPredictiveBackRunning = true
        try {
            events.collect { event ->
                requestedBackProgress = event.progress
                backSwipeEdge = event.swipeEdge
            }
            when (layer) {
                AndroidBackLayer.LYRICS -> lyricsVisible = false
                AndroidBackLayer.PLAYER -> playerVisible = false
            }
        } finally {
            isPredictiveBackRunning = false
            requestedBackProgress = 0f
        }
    }

    val systemConfiguration = LocalConfiguration.current
    val paletteColorScheme = remember(
        controller.palette,
        controller.isDark,
        systemConfiguration,
    ) {
        when (val palette = controller.palette) {
            LazerPalette.Default -> null
            LazerPalette.System -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (controller.isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                null
            }
            is LazerPalette.Custom -> seedColorScheme(palette.seed, controller.isDark)
        }
    }

    val isGlass = controller.skin == AppSkin.GLASS
    val playFromQueue: (List<AndroidTrack>, AndroidTrack) -> Unit = { queue, track ->
        AndroidPlaybackConnection.play(context, queue, track)
    }
    val overlays: @Composable BoxScope.() -> Unit = {
        controller.message?.let { text ->
            MessageBanner(text, Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(16.dp))
        }
        AnimatedVisibility(
            visible = playerVisible && playback.track != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInVertically(
                animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                initialOffsetY = { height -> height / 8 },
            ) + fadeIn(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing)),
            exit = slideOutVertically(
                animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                targetOffsetY = { height -> height / 8 },
            ) + fadeOut(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing)),
            label = "now-playing-page",
        ) {
            NowPlayingPage(
                snapshot = playback,
                lyricLines = controller.lyrics,
                lyricsLoading = controller.lyricsLoading,
                lyricsMessage = controller.lyricsMessage,
                lyricFollowDelayMillis = controller.lyricFollowDelayMillis,
                lyricAnimationSpeed = controller.lyricAnimationSpeed,
                wordLyricsEnabled = controller.wordLyricsEnabled,
                lyricGlowEnabled = controller.lyricGlowEnabled,
                liquidGlassEnabled = controller.liquidGlassEnabled,
                lyricFontSizeSp = controller.lyricFontSizeSp,
                showFullLyrics = controller.showFullLyrics,
                isLiked = playback.track?.let { controller.isSongLiked(it.id) } == true,
                onToggleLiked = { playback.track?.let(controller::toggleSongLiked) },
                onDismiss = { playerVisible = false },
                onToggle = { AndroidPlaybackConnection.toggle(context) },
                onPrevious = { AndroidPlaybackConnection.previous(context) },
                onNext = { AndroidPlaybackConnection.next(context) },
                onSeek = { AndroidPlaybackConnection.seekTo(context, it) },
                onLyrics = { lyricsVisible = true },
                glass = isGlass,
                modifier = Modifier
                    .fillMaxSize()
                    .predictiveBackTransform(
                        enabled = transformedBackLayer == AndroidBackLayer.PLAYER,
                        progress = renderedBackProgress,
                        swipeEdge = backSwipeEdge,
                    ),
            )
        }
        AnimatedVisibility(
            visible = lyricsVisible && playback.track != null,
            modifier = Modifier.fillMaxSize(),
            enter = slideInHorizontally(
                animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                initialOffsetX = { width -> width / 5 },
            ) + fadeIn(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing)),
            exit = slideOutHorizontally(
                animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                targetOffsetX = { width -> width / 5 },
            ) + fadeOut(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing)),
            label = "lyrics-page",
        ) {
            AndroidLyricsPage(
                track = playback.track,
                lines = controller.lyrics,
                isLoading = controller.lyricsLoading,
                message = controller.lyricsMessage,
                positionMillis = playback.positionMillis,
                followDelayMillis = controller.lyricFollowDelayMillis,
                animationSpeed = controller.lyricAnimationSpeed,
                wordLyricsEnabled = controller.wordLyricsEnabled,
                lyricGlowEnabled = controller.lyricGlowEnabled,
                lyricFontSizeSp = controller.lyricFontSizeSp,
                showFullLyrics = controller.showFullLyrics,
                onBack = { lyricsVisible = false },
                onSeek = { AndroidPlaybackConnection.seekTo(context, it) },
                useGlassBackground = isGlass,
                modifier = Modifier
                    .fillMaxSize()
                    .predictiveBackTransform(
                        enabled = transformedBackLayer == AndroidBackLayer.LYRICS,
                        progress = renderedBackProgress,
                        swipeEdge = backSwipeEdge,
                    ),
            )
        }
        if ((context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
            // Top-end, just below the header row, so it never overlaps the bottom controls.
            DebugWatermark(
                enabled = true,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 54.dp, end = 14.dp),
            )
        }
    }
    if (isGlass) {
        // Pure white stage, always: light palette + solid white background, regardless of
        // system theme — that is the glass skin's identity.
        GlassTheme(darkTheme = false, amoled = false) {
            // VibeUsage layering: page orbs backdrop -> captured content layer -> floating glass
            // (mini player / bottom tabs) samples the combined scrim so scrolling content blurs
            // live behind it. Content-layer glass must only ever sample the page backdrop.
            val pageBackdrop = rememberPageBackdrop()
            val contentBackdrop = kyantRememberLayerBackdrop()
            val scrimBackdrop = kyantRememberCombinedBackdrop(pageBackdrop, contentBackdrop)
            CompositionLocalProvider(
                LocalGlassEnv provides GlassEnv(pageBackdrop, contentBackdrop, scrimBackdrop),
                LocalAndroidContentBottomInset provides ((if (playback.track != null) 76.dp else 0.dp) + 116.dp),
            ) {
                Box(Modifier.fillMaxSize()) {
                    GlassBackground(pageBackdrop)
                    Box(Modifier.fillMaxSize().kyantLayerBackdrop(contentBackdrop)) {
                        AndroidRootContent(
                            controller = controller,
                            currentTrackId = playback.track?.id,
                            onPlay = playFromQueue,
                            isPlaying = playback.isPlaying,
                            modifier = Modifier.fillMaxSize().statusBarsPadding(),
                        )
                    }
                    Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                        playback.track?.let { track ->
                            GlassMiniPlayer(
                                track = track,
                                isPlaying = playback.isPlaying,
                                isPreparing = playback.isPreparing,
                                backdrop = scrimBackdrop,
                                onOpen = { playerVisible = true },
                                onToggle = { AndroidPlaybackConnection.toggle(context) },
                            )
                        }
                        Box(
                            Modifier
                                .padding(start = 16.dp, end = 16.dp)
                                .padding(bottom = navigationBarBottomInset() + 14.dp),
                        ) {
                            LiquidBottomTabs(
                                selectedTabIndex = { controller.destination.ordinal },
                                onTabSelected = { index ->
                                    controller.selectDestination(AndroidRootDestination.entries[index])
                                },
                                backdrop = scrimBackdrop,
                                tabsCount = AndroidRootDestination.entries.size,
                            ) {
                                AndroidRootDestination.entries.forEach { destination ->
                                    LiquidBottomTab({ controller.selectDestination(destination) }) {
                                        Icon(destination.icon(), null, Modifier.size(22.dp), tint = LocalGlassPalette.current.InkHi)
                                        Text(destination.label, style = TextStyle(LocalGlassPalette.current.InkHi, 11.sp))
                                    }
                                }
                            }
                        }
                    }
                    overlays()
                }
            }
        }
        return
    }
    LazerTheme(
        isDark = controller.isDark,
        colorScheme = paletteColorScheme,
        engine = controller.themeEngine,
    ) {
        val colors = MaterialTheme.colorScheme
        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                (view.context as? Activity)?.window?.let { window ->
                    WindowCompat.getInsetsController(window, view).apply {
                        isAppearanceLightStatusBars = !controller.isDark
                        isAppearanceLightNavigationBars = !controller.isDark
                    }
                }
            }
        }
        // A custom background wallpaper is drawn fully opaque; the slider fades the app's own
        // surfaces above it (see LocalLazerUiAlpha), never the wallpaper.
        val wallpaper = controller.backgroundImage.takeIf { controller.backgroundImageEnabled }
        val hasWallpaper = wallpaper != null
        val uiAlpha = if (hasWallpaper) controller.backgroundAlpha else 1f
        Box(Modifier.fillMaxSize().background(colors.background)) {
            wallpaper?.let { image ->
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            // Global scrim, always present and constant: it dims the wallpaper and covers every
            // pixel a page might vacate during a return, so the raw image is never exposed at full
            // opacity. Pages additionally carry their own scrim (below) so their background moves
            // with them; the global layer sits behind the pages and is what shows in the gaps.
            Box(
                Modifier
                    .fillMaxSize()
                    .background(colors.background.copy(alpha = uiAlpha)),
            )
            val liquidGlass = rememberLazerLiquidGlass(controller.liquidGlassEnabled, colors.background)
            val floatingControlsInset = if (liquidGlass.isEnabled) {
                (if (playback.track != null) 76.dp else 0.dp) + 96.dp
            } else {
                0.dp
            }
            // Android 16 forces edge-to-edge. Keep the visual canvas under the status bar, while
            // placing every interactive root-page element below its dynamic inset.
            CompositionLocalProvider(
                LocalAndroidContentBottomInset provides floatingControlsInset,
                LocalLazerUiAlpha provides uiAlpha,
            ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .captureLiquidGlass(liquidGlass),
            ) {
                // Manual status-bar inset. It paints the same scrim as every page, so the top
                // safe area matches the content below it exactly.
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(statusBarTopInset())
                        .background(colors.background.copy(alpha = uiAlpha)),
                )
                Box(Modifier.weight(1f)) {
                    AndroidRootContent(
                        controller = controller,
                        currentTrackId = playback.track?.id,
                        onPlay = playFromQueue,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (!liquidGlass.isEnabled) {
                    playback.track?.let { track ->
                        MiniPlayer(track, playback.isPlaying, playback.isPreparing, { playerVisible = true }) {
                            AndroidPlaybackConnection.toggle(context)
                        }
                    }
                    BottomDock(
                        selected = controller.destination,
                        onSelect = controller::selectDestination,
                    )
                }
            }
            }

            if (liquidGlass.isEnabled) {
                Column(Modifier.align(Alignment.BottomCenter).fillMaxWidth()) {
                    playback.track?.let { track ->
                        MiniPlayer(track, playback.isPlaying, playback.isPreparing, { playerVisible = true }) {
                            AndroidPlaybackConnection.toggle(context)
                        }
                    }
                    LiquidGlassBottomDock(
                        selected = controller.destination,
                        onSelect = controller::selectDestination,
                        glass = liquidGlass,
                    )
                }
            }

            overlays()
        }
    }
}

@Composable
private fun AndroidRootContent(
    controller: AndroidLibraryController,
    currentTrackId: Long?,
    onPlay: (List<AndroidTrack>, AndroidTrack) -> Unit,
    isPlaying: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val fileImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments(),
    ) { uris -> controller.importFromUris(uris) }
    var wifiSheetVisible by remember { mutableStateOf(false) }
    var webdavSheetVisible by remember { mutableStateOf(false) }
    var importOptionsVisible by remember { mutableStateOf(false) }

    Column(modifier) {
        MobileHeader(
            controller = controller,
            modifier = Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp, bottom = 6.dp),
        )
        AnimatedContent(
            targetState = controller.destination,
            modifier = Modifier.weight(1f),
            transitionSpec = {
                val movesForward = targetState.motionIndex > initialState.motionIndex
                val enter = slideInHorizontally(
                    animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                    initialOffsetX = { width -> if (movesForward) width / 5 else -width / 5 },
                ) + fadeIn(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing))
                val exit = slideOutHorizontally(
                    animationSpec = tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing),
                    targetOffsetX = { width -> if (movesForward) -width / 8 else width / 8 },
                ) + fadeOut(tween(PAGE_TRANSITION_MILLIS, easing = LazerMotionEasing))
                enter togetherWith exit
            },
            label = "android-root",
        ) { destination ->
            when (destination) {
                AndroidRootDestination.LIBRARY -> LibraryPage(
                    controller = controller,
                    currentId = currentTrackId,
                    isPlaying = isPlaying,
                    onPlay = { queue, track -> onPlay(queue, track) },
                    onOpenImport = { importOptionsVisible = true },
                    onSelectFiles = { fileImportLauncher.launch(arrayOf("audio/*", "*/*")) },
                    onSelectWifi = { wifiSheetVisible = true },
                    onSelectWebdav = { webdavSheetVisible = true },
                )
                AndroidRootDestination.SEARCH -> SearchPage(
                    controller,
                    currentTrackId,
                    isPlaying = isPlaying,
                    onPlay = { queue, track -> onPlay(queue, track) },
                )
                AndroidRootDestination.SETTINGS -> SettingsPage(
                    controller = controller,
                    onOpenImport = { importOptionsVisible = true },
                )
            }
        }
    }

    if (importOptionsVisible) {
        ImportOptionsSheet(
            onSelectFiles = { fileImportLauncher.launch(arrayOf("audio/*", "*/*")) },
            onSelectWifi = { wifiSheetVisible = true },
            onSelectWebdav = { webdavSheetVisible = true },
            onDismiss = { importOptionsVisible = false },
        )
    }
    if (wifiSheetVisible) {
        WifiImportSheet(controller, onDismiss = { wifiSheetVisible = false })
    }
    if (webdavSheetVisible) {
        WebDavSheet(controller, onDismiss = { webdavSheetVisible = false })
    }
}

@Composable
private fun LibraryPage(
    controller: AndroidLibraryController,
    currentId: Long?,
    onPlay: (List<AndroidTrack>, AndroidTrack) -> Unit,
    onOpenImport: () -> Unit,
    onSelectFiles: () -> Unit,
    onSelectWifi: () -> Unit,
    onSelectWebdav: () -> Unit,
    isPlaying: Boolean = false,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 18.dp + LocalAndroidContentBottomInset.current),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            val colors = MaterialTheme.colorScheme
            Row(
                Modifier.widthIn(max = 470.dp).fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(tr("library.title"), style = MaterialTheme.typography.displaySmall)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (controller.internalTracks.isEmpty()) tr("library.subtitle") else tr("library.count", controller.internalTracks.size),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Surface(
                    onClick = onOpenImport,
                    shape = RoundedCornerShape(20.dp),
                    color = colors.primary,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = colors.onPrimary,
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = tr("internal.import"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.onPrimary,
                        )
                    }
                }
            }
        }
        item {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                AndroidLibraryFilter.entries.forEach { filter ->
                    ThemeTextButton(onClick = { controller.updateLibraryFilter(filter) }) {
                        Text(
                            filter.label,
                            color = if (controller.libraryFilter == filter) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontWeight = if (controller.libraryFilter == filter) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                SettingsDropdown(
                    options = AndroidLibrarySort.entries,
                    selected = controller.librarySort,
                    label = AndroidLibrarySort::label,
                    onSelected = controller::updateLibrarySort,
                    icon = AndroidLibrarySort::icon,
                )
            }
        }
        when {
            controller.isInternalScanning && controller.internalTracks.isEmpty() ->
                item { QuietState(tr("library.scanning")) }
            controller.internalTracks.isEmpty() -> item {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    QuietState(tr("internal.empty"))
                    Spacer(Modifier.height(18.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        GlassActionButton(
                            tr("internal.files"),
                            onSelectFiles,
                            LocalGlassEnv.current?.page,
                            accent = true,
                        )
                        GlassActionButton(
                            tr("internal.wifi"),
                            onSelectWifi,
                            LocalGlassEnv.current?.page,
                        )
                        GlassActionButton(
                            tr("internal.webdav"),
                            onSelectWebdav,
                            LocalGlassEnv.current?.page,
                        )
                    }
                }
            }
            controller.displayedTracks.isEmpty() -> item {
                QuietState(
                    when (controller.libraryFilter) {
                        AndroidLibraryFilter.FAVORITES -> tr("library.favorites.empty")
                        AndroidLibraryFilter.RECENT -> tr("library.recent.empty")
                        AndroidLibraryFilter.ALL -> tr("internal.empty")
                    },
                )
            }
            else -> items(controller.displayedTracks, key = AndroidTrack::id) { track ->
                TrackRow(
                    track,
                    track.id == currentId,
                    onClick = { onPlay(controller.displayedTracks, track) },
                    onLongClick = { controller.deleteInternalTrack(track) },
                )
            }
        }
    }
}

@Composable
private fun SearchPage(
    controller: AndroidLibraryController,
    currentId: Long?,
    onPlay: (List<AndroidTrack>, AndroidTrack) -> Unit,
    isPlaying: Boolean = false,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 12.dp, 20.dp, 18.dp + LocalAndroidContentBottomInset.current),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item { Text(tr("search.title"), style = MaterialTheme.typography.displaySmall) }
        item {
            OutlinedTextField(
                value = controller.searchQuery,
                onValueChange = controller::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                placeholder = { Text(tr("search.hint")) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            )
        }
        when {
            controller.searchQuery.isBlank() -> item { QuietState(tr("search.empty")) }
            controller.searchResults.isEmpty() -> item { QuietState(tr("search.no_results")) }
            else -> items(controller.searchResults, key = AndroidTrack::id) { track ->
                TrackRow(track, track.id == currentId, onClick = { onPlay(controller.searchResults, track) }, isPlaying = isPlaying)
            }
        }
    }
}

@Composable
private fun SettingsPage(
    controller: AndroidLibraryController,
    onOpenImport: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlassEnv.current != null
    val systemMonetAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val backgroundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(controller::setBackgroundImage) }
    var followDelaySliderValue by remember(controller.lyricFollowDelayMillis) {
        mutableFloatStateOf(controller.lyricFollowDelayMillis.toFloat())
    }
    var lyricFontSizeSliderValue by remember(controller.lyricFontSizeSp) {
        mutableFloatStateOf(controller.lyricFontSizeSp.toFloat())
    }
    val displayedFollowDelay = normalizeLyricFollowDelayMillis(followDelaySliderValue.roundToLong())
    val displayedLyricFontSize = normalizeLyricFontSizeSp(lyricFontSizeSliderValue.roundToInt())
    val animationSpeedOptions = LyricAnimationSpeed.entries
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp, 10.dp, 20.dp, 18.dp + LocalAndroidContentBottomInset.current),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text(tr("settings.title"), style = MaterialTheme.typography.headlineSmall)
        }
        item { SectionTitle(tr("settings.library")) }
        item {
            SettingsCard {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.library"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (controller.isInternalScanning) {
                                tr("library.scanning")
                            } else if (controller.internalTracks.isEmpty()) {
                                tr("internal.empty")
                            } else {
                                tr("library.count", controller.internalTracks.size)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    GlassActionButton(
                        tr("internal.import"),
                        onOpenImport,
                        LocalGlassEnv.current?.page,
                        accent = true,
                    )
                }
            }
        }
        item { SectionTitle(tr("settings.appearance")) }
        item {
            SettingsCard {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.skin"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            interfaceStyleLabel(currentInterfaceOption(controller)),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    SettingsDropdown(
                        options = InterfaceStyleOption.entries,
                        selected = currentInterfaceOption(controller),
                        label = ::interfaceStyleLabel,
                        onSelected = { option -> applyInterfaceOption(controller, option) },
                        icon = InterfaceStyleOption::icon,
                    )
                }
            }
        }
        if (!glass) item {
            SettingsCard(
                modifier = Modifier.clickable(role = Role.Switch) {
                    controller.toggleTheme()
                },
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.interface.title"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (controller.isDark) tr("settings.interface.dark") else tr("settings.interface.light"),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    SettingsSwitch(
                        checked = controller.isDark,
                        onCheckedChange = { controller.toggleTheme() },
                    )
                }
            }
        }
        if (!glass) item {
            val paletteOptions = buildList {
                add(LazerPalette.Default)
                if (systemMonetAvailable) add(LazerPalette.System)
                add(LazerPalette.Custom(LazerSeedSwatches.first()))
            }
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("settings.palette"), style = MaterialTheme.typography.titleSmall)
                            Text(
                                paletteLabel(controller.palette),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        SettingsDropdown(
                            options = paletteOptions,
                            selected = controller.palette,
                            label = ::paletteLabel,
                            onSelected = controller::updatePalette,
                            icon = { Icons.Outlined.ColorLens },
                        )
                    }
                    val custom = controller.palette
                    if (custom is LazerPalette.Custom) {
                        Spacer(Modifier.height(12.dp))
                        SeedColorPicker(
                            seed = custom.seed,
                            onSeedChange = { controller.updatePalette(LazerPalette.Custom(it)) },
                        )
                    }
                }
            }
        }
        item {
            SettingsCard {
                Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.language"), style = MaterialTheme.typography.titleSmall)
                        Text(controller.language.displayName, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                    }
                    LanguageDropdown(
                        selected = controller.language,
                        onSelected = controller::updateLanguage,
                    )
                }
            }
        }
        if (!glass) item {
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("settings.background"), style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (controller.backgroundImage != null) tr("settings.background.change") else tr("settings.background.none"),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        if (controller.backgroundImage != null) {
                            SettingsSwitch(
                                checked = controller.backgroundImageEnabled,
                                onCheckedChange = controller::updateBackgroundImageEnabled,
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        ThemeTextButton(onClick = { backgroundPicker.launch("image/*") }) {
                            Text(tr("settings.background.pick"))
                        }
                    }
                    if (controller.backgroundImage != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(tr("settings.background.alpha"), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            LazerSlider(
                                engine = if (glass) LazerThemeEngine.MATERIAL3 else controller.themeEngine,
                                value = controller.backgroundAlpha,
                                onValueChange = controller::updateBackgroundAlpha,
                                valueRange = 0f..1f,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("${(controller.backgroundAlpha * 100).roundToInt()}%", style = MaterialTheme.typography.labelMedium, color = colors.primary)
                        }
                        ThemeTextButton(onClick = controller::clearBackgroundImage) {
                            Text(tr("settings.background.clear"), color = colors.error)
                        }
                    }
                }
            }
        }
        item { SectionTitle(tr("settings.lyrics")) }
        item {
            SettingsCard(
                modifier = Modifier.clickable(role = Role.Switch) {
                    controller.updateWordLyricsEnabled(!controller.wordLyricsEnabled)
                },
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.word.title"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (controller.wordLyricsEnabled) tr("settings.word.on") else tr("settings.word.off"),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    SettingsSwitch(checked = controller.wordLyricsEnabled, onCheckedChange = controller::updateWordLyricsEnabled)
                }
            }
        }
        item {
            SettingsCard(
                modifier = Modifier.clickable(role = Role.Switch) {
                    controller.updateLyricGlowEnabled(!controller.lyricGlowEnabled)
                },
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.lyric.glow.title"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (controller.lyricGlowEnabled) tr("settings.lyric.glow.on") else tr("settings.lyric.glow.off"),
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    SettingsSwitch(checked = controller.lyricGlowEnabled, onCheckedChange = controller::updateLyricGlowEnabled)
                }
            }
        }
        item {
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("settings.lyric.speed.title"), style = MaterialTheme.typography.titleSmall)
                            Text(
                                tr("settings.lyric.speed.hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            controller.lyricAnimationSpeed.label,
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                    LazerSlider(
                        engine = if (glass) LazerThemeEngine.MATERIAL3 else controller.themeEngine,
                        value = controller.lyricAnimationSpeed.ordinal.toFloat(),
                        onValueChange = { value ->
                            controller.updateLyricAnimationSpeed(
                                animationSpeedOptions[value.roundToInt().coerceIn(animationSpeedOptions.indices)],
                            )
                        },
                        valueRange = 0f..animationSpeedOptions.lastIndex.toFloat(),
                        steps = animationSpeedOptions.size - 2,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            animationSpeedOptions.first().label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            LyricAnimationSpeed.STANDARD.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            animationSpeedOptions.last().label,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("settings.lyric.font.title"), style = MaterialTheme.typography.titleSmall)
                            Text(
                                tr("settings.lyric.font.hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            lyricFontSizeLabel(displayedLyricFontSize),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                    LazerSlider(
                        engine = if (glass) LazerThemeEngine.MATERIAL3 else controller.themeEngine,
                        value = lyricFontSizeSliderValue,
                        onValueChange = {
                            lyricFontSizeSliderValue = normalizeLyricFontSizeSp(it.roundToInt()).toFloat()
                        },
                        onValueChangeFinished = {
                            controller.updateLyricFontSizeSp(displayedLyricFontSize)
                        },
                        valueRange = MIN_LYRIC_FONT_SIZE_SP.toFloat()..MAX_LYRIC_FONT_SIZE_SP.toFloat(),
                        steps = LYRIC_FONT_SIZE_OPTIONS_SP.size - 2,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            tr("settings.lyric.font.small"),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            tr("settings.lyric.font.large"),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        item {
            SettingsCard(
                modifier = Modifier.clickable(role = Role.Switch) {
                    controller.updateShowFullLyrics(!controller.showFullLyrics)
                },
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(tr("settings.lyric.full.title"), style = MaterialTheme.typography.titleSmall)
                        Text(
                            if (controller.showFullLyrics) {
                                tr("settings.lyric.full.on")
                            } else {
                                tr("settings.lyric.full.off")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    SettingsSwitch(checked = controller.showFullLyrics, onCheckedChange = controller::updateShowFullLyrics)
                }
            }
        }
        item {
            SettingsCard {
                Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(tr("settings.lyric.follow.title"), style = MaterialTheme.typography.titleSmall)
                            Text(
                                tr("settings.lyric.follow.hint"),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        Text(
                            lyricFollowDelayLabel(displayedFollowDelay),
                            style = MaterialTheme.typography.labelLarge,
                            color = colors.primary,
                        )
                    }
                    LazerSlider(
                        engine = if (glass) LazerThemeEngine.MATERIAL3 else controller.themeEngine,
                        value = followDelaySliderValue,
                        onValueChange = {
                            followDelaySliderValue = normalizeLyricFollowDelayMillis(it.roundToLong()).toFloat()
                        },
                        onValueChangeFinished = {
                            controller.updateLyricFollowDelay(displayedFollowDelay)
                        },
                        valueRange = MIN_LYRIC_FOLLOW_DELAY_MILLIS.toFloat()..MAX_LYRIC_FOLLOW_DELAY_MILLIS.toFloat(),
                        steps = LYRIC_FOLLOW_DELAY_OPTIONS_MILLIS.size - 2,
                    )
                    Row(Modifier.fillMaxWidth()) {
                        Text(
                            lyricFollowDelayLabel(MIN_LYRIC_FOLLOW_DELAY_MILLIS),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            lyricFollowDelayLabel(MAX_LYRIC_FOLLOW_DELAY_MILLIS),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun MusicSource.icon(): ImageVector = when (this) {
    MusicSource.DEVICE -> Icons.Outlined.Smartphone
    MusicSource.INTERNAL -> Icons.Outlined.FolderSpecial
}

private fun AndroidLibrarySort.icon(): ImageVector = when (this) {
    AndroidLibrarySort.TITLE -> Icons.Outlined.SortByAlpha
    AndroidLibrarySort.ARTIST -> Icons.Outlined.Person
    AndroidLibrarySort.ALBUM -> Icons.Outlined.Album
}

private fun LazerStyle.icon(): ImageVector = when (this) {
    LazerStyle.MATERIAL -> Icons.Outlined.Palette
    LazerStyle.MIUIX -> Icons.Outlined.Widgets
    LazerStyle.LIQUID_GLASS -> Icons.Outlined.WaterDrop
}

@Composable
private fun <T> SettingsDropdown(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    icon: ((T) -> ImageVector?)? = null,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "dropdown_arrow",
    )

    val triggerInteraction = remember { MutableInteractionSource() }
    val isPressed by triggerInteraction.collectIsPressedAsState()
    val pillScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "dropdown_pill_scale",
    )

    val triggerBgColor = if (isDark) {
        if (expanded) Color(0xFF2C3642) else Color(0xFF222B34)
    } else {
        if (expanded) Color(0xFFF1F5F9) else Color(0xFFFFFFFF)
    }
    val triggerBorderColor = if (expanded) {
        colors.primary
    } else {
        if (isDark) Color(0xFF3B4856) else Color(0xFFD2DCE6)
    }

    Box(modifier) {
        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                expanded = !expanded
            },
            shape = RoundedCornerShape(20.dp),
            color = triggerBgColor,
            shadowElevation = if (isPressed) 1.dp else if (expanded) 3.dp else 1.5.dp,
            border = BorderStroke(
                width = 1.2.dp,
                color = triggerBorderColor,
            ),
            interactionSource = triggerInteraction,
            modifier = Modifier.graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
            },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val selectedIcon = icon?.invoke(selected)
                if (selectedIcon != null) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = colors.primary,
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    text = label(selected),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFFECEFF4) else Color(0xFF1E293B),
                    maxLines = 1,
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = arrowRotation },
                    tint = colors.primary,
                )
            }
        }

        val menuBgColor = if (isDark) Color(0xFF202830) else Color(0xFFFFFFFF)
        val menuBorderColor = if (isDark) Color(0xFF384654) else Color(0xFFD6DEE7)

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 0.dp, y = 8.dp),
            shape = RoundedCornerShape(18.dp),
            containerColor = menuBgColor,
            tonalElevation = 0.dp,
            shadowElevation = 16.dp,
            border = BorderStroke(1.2.dp, menuBorderColor),
            modifier = Modifier
                .background(menuBgColor, RoundedCornerShape(18.dp))
                .widthIn(min = if (icon != null) 168.dp else 146.dp),
        ) {
            Spacer(Modifier.height(5.dp))
            options.forEach { option ->
                val isSelected = option == selected
                val optionIcon = icon?.invoke(option)
                val itemInteraction = remember { MutableInteractionSource() }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                if (isDark) Color(0xFF2C3946) else Color(0xFFEEF4F8)
                            } else {
                                menuBgColor
                            }
                        )
                        .clickable(
                            interactionSource = itemInteraction,
                            indication = androidx.compose.material3.ripple(),
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onSelected(option)
                                expanded = false
                            },
                        )
                        .padding(horizontal = 10.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (optionIcon != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) {
                                        if (isDark) Color(0xFF3B4D5E) else Color(0xFFDCE8F2)
                                    } else {
                                        if (isDark) Color(0xFF27313B) else Color(0xFFF1F5F8)
                                    }
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = optionIcon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isSelected) colors.primary else colors.onSurfaceVariant,
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(
                        text = label(option),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (isSelected) colors.primary else (if (isDark) Color(0xFFE2E8F0) else Color(0xFF1E293B)),
                        modifier = Modifier.weight(1f),
                    )
                    if (isSelected) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(colors.primary),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = colors.onPrimary,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(5.dp))
        }
    }
}

/** One merged entry of the interface-style dropdown (skin + paper sub-style). */
private enum class InterfaceStyleOption { GLASS, PAPER_MATERIAL, PAPER_MIUIX, PAPER_LIQUID_GLASS }

private fun currentInterfaceOption(controller: AndroidLibraryController): InterfaceStyleOption = when {
    controller.skin == AppSkin.GLASS -> InterfaceStyleOption.GLASS
    controller.style == LazerStyle.MIUIX -> InterfaceStyleOption.PAPER_MIUIX
    controller.style == LazerStyle.LIQUID_GLASS -> InterfaceStyleOption.PAPER_LIQUID_GLASS
    else -> InterfaceStyleOption.PAPER_MATERIAL
}

private fun interfaceStyleLabel(option: InterfaceStyleOption): String = when (option) {
    InterfaceStyleOption.GLASS -> tr("skin.glass")
    InterfaceStyleOption.PAPER_MATERIAL -> LazerStyle.MATERIAL.label
    InterfaceStyleOption.PAPER_MIUIX -> LazerStyle.MIUIX.label
    InterfaceStyleOption.PAPER_LIQUID_GLASS -> LazerStyle.LIQUID_GLASS.label
}

private fun InterfaceStyleOption.icon(): ImageVector = when (this) {
    InterfaceStyleOption.GLASS -> Icons.Outlined.AutoAwesome
    InterfaceStyleOption.PAPER_MATERIAL -> Icons.Outlined.Palette
    InterfaceStyleOption.PAPER_MIUIX -> Icons.Outlined.Widgets
    InterfaceStyleOption.PAPER_LIQUID_GLASS -> Icons.Outlined.WaterDrop
}

private fun applyInterfaceOption(controller: AndroidLibraryController, option: InterfaceStyleOption) {
    when (option) {
        InterfaceStyleOption.GLASS -> controller.updateSkin(AppSkin.GLASS)
        InterfaceStyleOption.PAPER_MATERIAL -> {
            controller.updateSkin(AppSkin.PAPER)
            controller.updateStyle(LazerStyle.MATERIAL)
        }
        InterfaceStyleOption.PAPER_MIUIX -> {
            controller.updateSkin(AppSkin.PAPER)
            controller.updateStyle(LazerStyle.MIUIX)
        }
        InterfaceStyleOption.PAPER_LIQUID_GLASS -> {
            controller.updateSkin(AppSkin.PAPER)
            controller.updateStyle(LazerStyle.LIQUID_GLASS)
        }
    }
}

private fun paletteLabel(palette: LazerPalette): String = when (palette) {
    LazerPalette.Default -> tr("settings.palette.default")
    LazerPalette.System -> tr("settings.palette.system")
    is LazerPalette.Custom -> tr("settings.palette.custom")
}

@Composable
private fun StyleDropdown(selected: LazerStyle, onSelected: (LazerStyle) -> Unit) {
    SettingsDropdown(
        options = LazerStyle.entries,
        selected = selected,
        label = LazerStyle::label,
        onSelected = onSelected,
        icon = LazerStyle::icon,
    )
}

@Composable
private fun LanguageDropdown(selected: LazerLanguage, onSelected: (LazerLanguage) -> Unit) {
    SettingsDropdown(
        options = LazerLanguage.entries,
        selected = selected,
        label = LazerLanguage::displayName,
        onSelected = onSelected,
        icon = { Icons.Outlined.Translate },
    )
}

@Composable
private fun MobileHeader(controller: AndroidLibraryController, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Music Hub", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
        }
        if (LocalGlassEnv.current == null) {
            IconButton(onClick = controller::toggleTheme) { Icon(if (controller.isDark) Icons.Outlined.LightMode else Icons.Outlined.DarkMode, tr("player.toggle_theme")) }
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun TrackRow(
    track: AndroidTrack,
    current: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    isPlaying: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val glass = LocalGlassEnv.current != null
    val palette = LocalGlassPalette.current
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp))
            .background(
                when {
                    current && glass -> palette.AccentWash
                    current -> colors.primaryContainer.copy(alpha = 0.58f)
                    else -> Color.Transparent
                },
            )
            .then(
                if (current && glass) {
                    Modifier.border(1.dp, palette.AccentRim, RoundedCornerShape(15.dp))
                } else {
                    Modifier
                },
            )
            .combinedClickable(
                role = Role.Button,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MobileArtwork(track.coverUrl, track.title, Modifier.size(48.dp), 12.dp)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = Color.Unspecified,
            )
            Text(listOf(track.displayArtist, track.album).filter(String::isNotBlank).joinToString(" · "), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (current && glass) {
            PlayingBars(accent = palette.Accent, animate = isPlaying, modifier = Modifier.padding(end = 10.dp))
        }
        Text(track.durationLabel, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
    }
}

/** Three tiny accent bars that dance while the highlighted track is playing. */
@Composable
private fun PlayingBars(accent: Color, animate: Boolean, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "playing-bars")
    val bases = listOf(0.40f, 1f, 0.65f)
    Row(
        modifier.height(14.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        bases.forEachIndexed { index, base ->
            val barFraction = if (animate) {
                transition.animateFloat(
                    initialValue = base,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(tween(380 + index * 120), RepeatMode.Reverse),
                    label = "bar" + index,
                ).value
            } else {
                base
            }
            Box(Modifier.size(3.dp, 14.dp * barFraction).clip(CircleShape).background(accent))
        }
    }
}

@Composable
private fun MobileArtwork(
    url: String?,
    label: String,
    modifier: Modifier,
    cornerRadius: androidx.compose.ui.unit.Dp,
    onClick: (() -> Unit)? = null,
) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(Brush.linearGradient(listOf(colors.primaryContainer, colors.secondaryContainer)))
            // Clickable inside the clip so the ripple is confined to the rounded artwork.
            .then(
                if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label.firstOrNull()?.toString().orEmpty(), style = MaterialTheme.typography.titleMedium, color = colors.onPrimaryContainer)
        if (!url.isNullOrBlank()) {
            AsyncImage(
                model = url,
                contentDescription = tr("artwork.cover", label),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Composable
private fun GlassMiniPlayer(
    track: AndroidTrack,
    isPlaying: Boolean,
    isPreparing: Boolean,
    backdrop: Backdrop,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val palette = LocalGlassPalette.current
    Box(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
            .height(64.dp)
            .glassRow(backdrop, cornerRadius = 24.dp)
            .clickable(role = Role.Button, onClick = onOpen),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(Modifier.padding(horizontal = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            MobileArtwork(track.coverUrl, track.title, Modifier.size(42.dp), 13.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (isPreparing) tr("player.preparing") else track.displayArtist,
                    style = MaterialTheme.typography.labelSmall,
                    color = palette.InkMid,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Box(
                Modifier
                    .size(42.dp)
                    .glassTile(backdrop, cornerRadius = 21.dp, tint = palette.Accent.copy(alpha = 0.30f))
                    .clickable(role = Role.Button, onClick = onToggle),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    if (isPlaying) tr("player.pause") else tr("player.play"),
                    Modifier.size(22.dp),
                    tint = palette.InkHi,
                )
            }
        }
    }
}

/** Circular liquid-glass icon button (VibeUsage IconGlassButton pattern). */
@Composable
private fun GlassIconButton(
    size: Dp,
    backdrop: Backdrop,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    tint: Color = LocalGlassPalette.current.InkHi,
) {
    Box(
        modifier
            .size(size)
            .glassTile(backdrop, cornerRadius = size / 2)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(iconSize), tint = tint)
    }
}

/**
 * Settings switch: Always renders the liquid glass switch [GlassSwitch] as requested.
 */
@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    GlassSwitch(checked = checked, onCheckedChange = onCheckedChange, modifier = modifier)
}

/** iOS / Liquid glass style capsule switch with fluid spring physics, tactile haptic feedback,
 * and liquid stretch thumb response. */
@Composable
private fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val haptic = LocalHapticFeedback.current
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    val fraction by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "glass-switch-fraction",
    )

    val thumbWidth by animateDpAsState(
        targetValue = if (isPressed) 29.dp else 26.dp,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 420f),
        label = "glass-switch-thumb-width",
    )

    val activeColor = if (LocalGlassEnv.current != null) LocalGlassPalette.current.Accent else colors.primary
    val inactiveColor = if (isDark) Color(0xFF333D48) else Color(0xFFE2E7ED)
    val trackBorderColor = if (!checked) {
        if (isDark) Color(0xFF455260) else Color(0xFFD1D8E0)
    } else {
        null
    }

    Box(
        modifier = modifier
            .size(width = 54.dp, height = 32.dp)
            .clip(CircleShape)
            .background(if (checked) activeColor else inactiveColor)
            .then(
                if (trackBorderColor != null) {
                    Modifier.border(1.dp, trackBorderColor, CircleShape)
                } else Modifier
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Switch,
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange?.invoke(!checked)
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        val maxTravel = 54.dp - 6.dp - thumbWidth
        val thumbOffset = 3.dp + maxTravel * fraction

        Box(
            Modifier
                .offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
                .size(width = thumbWidth, height = 26.dp)
                .shadow(elevation = if (isPressed) 4.dp else 2.5.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}
/**
 * Skin-aware action button: a VibeUsage LiquidButton under the glass skin, the paper theme's
 * button otherwise. [accent] renders the emphasized (filled) variant of each skin.
 */
@Composable
private fun GlassActionButton(
    text: String,
    onClick: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Boolean = false,
) {
    if (LocalGlassEnv.current != null) {
        // Solid pill: LiquidButton's backdrop sampling drifts inside scrolling lists, so the
        // glass skin uses a themed solid pill here instead.
        val palette = LocalGlassPalette.current
        androidx.compose.material3.Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = modifier,
            shape = RoundedCornerShape(percent = 50),
            color = if (accent) palette.AccentInk else Color.White,
            contentColor = if (accent) Color.White else palette.InkHi,
            border = if (accent) null else BorderStroke(1.dp, palette.Rim),
        ) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text, style = MaterialTheme.typography.labelLarge)
            }
        }
    } else if (accent) {
        ThemeButton(onClick = onClick, modifier = modifier, enabled = enabled, cornerRadius = 11.dp) {
            Text(text)
        }
    } else {
        ThemeTextButton(onClick = onClick, modifier = modifier, enabled = enabled) {
            Text(text)
        }
    }
}

@Composable
private fun MiniPlayer(
    track: AndroidTrack,
    isPlaying: Boolean,
    isPreparing: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().height(76.dp).clickable(role = Role.Button, onClick = onOpen),
        color = colors.surface.copy(alpha = 0.98f * LocalLazerUiAlpha.current),
        border = BorderStroke(1.dp, colors.outlineVariant.copy(alpha = 0.78f)),
    ) {
        Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            MobileArtwork(track.coverUrl, track.title, Modifier.size(48.dp), 12.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(track.title, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(if (isPreparing) tr("player.preparing") else track.displayArtist, style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            IconButton(onClick = onToggle, modifier = Modifier.size(42.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = colors.primaryContainer)) {
                Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (isPlaying) tr("player.pause") else tr("player.play"), tint = colors.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun BottomDock(
    selected: AndroidRootDestination,
    onSelect: (AndroidRootDestination) -> Unit,
) {
    if (LocalLazerThemeEngine.current == LazerThemeEngine.MIUIX) {
        MiuixNavigationBar(
            modifier = Modifier.fillMaxWidth(),
            showDivider = true,
            defaultWindowInsetsPadding = true,
            mode = MiuixNavigationBarDisplayMode.IconAndText,
        ) {
            AndroidRootDestination.entries.forEach { destination ->
                MiuixNavigationBarItem(
                    selected = selected == destination,
                    onClick = { onSelect(destination) },
                    icon = destination.icon(),
                    label = destination.label,
                )
            }
        }
        return
    }

    val colors = MaterialTheme.colorScheme
    // The gesture bar area is painted by this Surface itself (a manual Box, not navigationBarsPadding)
    // so a custom background wallpaper behind the app never clashes with a system nav-bar scrim.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.background.copy(alpha = LocalLazerUiAlpha.current),
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp), horizontalArrangement = Arrangement.SpaceAround) {
                AndroidRootDestination.entries.forEach { destination ->
                    val active = selected == destination
                    Column(
                        Modifier.clip(RoundedCornerShape(13.dp)).clickable(role = Role.Tab) { onSelect(destination) }
                            .padding(horizontal = 14.dp, vertical = 6.dp).semantics { contentDescription = destination.label },
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(destination.icon(), null, Modifier.size(20.dp), tint = if (active) colors.primary else colors.onSurfaceVariant)
                        Text(destination.label, style = MaterialTheme.typography.labelSmall, color = if (active) colors.primary else colors.onSurfaceVariant, fontWeight = if (active) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }
            // Manual bottom gesture-bar inset, painted by the same surface.
            Box(Modifier.fillMaxWidth().height(navigationBarBottomInset()))
        }
    }
}

@Composable
private fun navigationBarBottomInset(): Dp =
    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

@Composable
private fun statusBarTopInset(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

@Composable
private fun LiquidGlassBottomDock(
    selected: AndroidRootDestination,
    onSelect: (AndroidRootDestination) -> Unit,
    glass: LazerLiquidGlass,
) {
    val colors = MaterialTheme.colorScheme
    val pageBackdrop = glass.backdrop ?: return
    val isDark = colors.background.luminance() < 0.5f
    val destinations = AndroidRootDestination.entries
    val barShape = RoundedCornerShape(26.dp)
    val selectorShape = RoundedCornerShape(20.dp)
    val density = androidx.compose.ui.platform.LocalDensity.current
    // While a long press is active the droplet follows the finger; otherwise `pressCenter` is null
    // and the droplet rests under the selected tab.
    var pressCenter by remember { mutableStateOf<Offset?>(null) }
    var pressedDestination by remember { mutableStateOf<AndroidRootDestination?>(null) }
    // Set briefly on a tap while the droplet slides to the tapped tab. During the slide every icon
    // except the target drops behind the droplet, so only the target stays on top.
    var switchingTo by remember { mutableStateOf<AndroidRootDestination?>(null) }
    val activeDestination = pressedDestination ?: selected
    val activeIndex = destinations.indexOf(activeDestination).coerceAtLeast(0)
    // Captures the bar glass only, so the droplet can refract it as a second nested lens. The
    // droplet is a sibling of this layer, so there is no cycle.
    val dockBackdrop = rememberLayerBackdrop()
    val nestedGlass = rememberCombinedBackdrop(pageBackdrop, dockBackdrop)

    // Manual bottom gesture-bar inset instead of navigationBarsPadding, so the floating glass dock
    // and a custom wallpaper do not fight over the system nav-bar region.
    BoxWithConstraints(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp + navigationBarBottomInset()),
    ) {
        val slotWidth = maxWidth / destinations.size
        val slotWidthPx = with(density) { slotWidth.toPx() }
        val selectorWidth = slotWidth - 14.dp
        val selectorCenterX by animateDpAsState(
            targetValue = with(density) {
                (pressCenter?.x ?: (slotWidthPx * activeIndex + slotWidthPx / 2f)).toDp()
            },
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
            label = "selector-x",
        )

        // Render priority: 0 = above the glass, 1 = behind it (captured into dockBackdrop so the
        // droplet refracts it). At rest every icon is priority 0. While a long press is active, or
        // during a tap-to-switch, non-target icons become priority 1.
        LaunchedEffect(switchingTo) {
            if (switchingTo != null) {
                kotlinx.coroutines.delay(420)
                switchingTo = null
            }
        }
        val renderIcons: @Composable (visible: (AndroidRootDestination) -> Boolean) -> Unit = { visible ->
            Row(Modifier.fillMaxSize()) {
                destinations.forEach { destination ->
                    // Always keep the slot so both layers stay aligned; only the content toggles.
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .semantics {
                                contentDescription = destination.label
                                role = Role.Tab
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (visible(destination)) {
                            val active = destination == activeDestination
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                // Strong foreground contrast on the glass; the droplet itself
                                // already communicates selection, so the active tab uses onSurface.
                                Icon(destination.icon(), null, Modifier.size(20.dp), tint = if (active) colors.onSurface else colors.onSurfaceVariant)
                                Text(destination.label, style = MaterialTheme.typography.labelSmall, color = if (active) colors.onSurface else colors.onSurfaceVariant, fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal)
                            }
                        }
                    }
                }
            }
        }
        val priorityOne: (AndroidRootDestination) -> Boolean = when {
            pressCenter != null -> { _ -> true }
            switchingTo != null -> { destination -> destination != switchingTo }
            else -> { _ -> false }
        }
        val priorityZero: (AndroidRootDestination) -> Boolean = { destination -> !priorityOne(destination) }

        Box(
            Modifier
                .fillMaxWidth()
                .height(72.dp)
                // One gesture handler for the whole bar: quick release selects a tab, holding ~0.5s
                // starts the finger-following droplet. Per-item clickable is intentionally omitted
                // so it cannot swallow the long press.
                .pointerInput(slotWidthPx) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val downIndex = (down.position.x / slotWidthPx).toInt()
                            .coerceIn(0, destinations.lastIndex)
                        val longPress = awaitLongPressOrCancellation(down.id)
                        if (longPress == null) {
                            val target = destinations[downIndex]
                            switchingTo = target
                            onSelect(target)
                            return@awaitEachGesture
                        }
                        pressCenter = longPress.position
                        pressedDestination = destinations.getOrNull(
                            (longPress.position.x / slotWidthPx).toInt(),
                        ) ?: destinations[downIndex]
                        while (true) {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull { it.id == down.id } ?: break
                            if (!change.pressed) break
                            pressCenter = change.position
                            pressedDestination = destinations.getOrNull(
                                (change.position.x / slotWidthPx).toInt(),
                            ) ?: pressedDestination
                            change.consume()
                        }
                        pressedDestination?.let(onSelect)
                        pressCenter = null
                        pressedDestination = null
                    }
                },
        ) {
            // Layer 1: the thick glass bar. Captured into dockBackdrop for the droplet to sample.
            // While pressing, the icons live here too (priority 1) so the droplet can refract them.
            Box(Modifier.fillMaxSize().layerBackdrop(dockBackdrop)) {
                // Near-colourless thick glass: strong lens refraction + chromatic dispersion, a
                // very faint milky surface, ambient edge highlight and inner shadow for depth.
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBackdrop(
                            backdrop = pageBackdrop,
                            shape = { barShape },
                            effects = {
                                vibrancy()
                                blur(10.dp.toPx())
                                lens(
                                    refractionHeight = 26.dp.toPx(),
                                    refractionAmount = 52.dp.toPx(),
                                    depthEffect = true,
                                    chromaticAberration = true,
                                )
                            },
                            highlight = { Highlight.Ambient },
                            innerShadow = { InnerShadow(radius = 14.dp, color = Color.Black.copy(alpha = 0.10f)) },
                            onDrawSurface = { drawRect(Color.White.copy(alpha = if (isDark) 0.06f else 0.10f)) },
                        ),
                )
                renderIcons(priorityOne)
            }

            // Layer 2: the selected droplet. Always an empty lens; it never draws an icon, so there
            // is nothing to fly in when it returns to its slot.
            Box(
                Modifier
                    .offset {
                        IntOffset(
                            x = (selectorCenterX.toPx() - selectorWidth.toPx() / 2f).roundToInt(),
                            y = ((72.dp - 56.dp).toPx() / 2f).roundToInt(),
                        )
                    }
                    .size(selectorWidth, 56.dp)
                    .drawBackdrop(
                        backdrop = nestedGlass,
                        shape = { selectorShape },
                        effects = {
                            vibrancy()
                            blur(2.5f.dp.toPx())
                            lens(
                                refractionHeight = 22.dp.toPx(),
                                refractionAmount = 64.dp.toPx(),
                                depthEffect = true,
                                chromaticAberration = true,
                            )
                        },
                        highlight = { Highlight.Default },
                        innerShadow = { InnerShadow(radius = 10.dp, color = Color.Black.copy(alpha = 0.08f)) },
                        onDrawSurface = { drawRect(Color.White.copy(alpha = if (isDark) 0.05f else 0.09f)) },
                    ),
            )

            // Layer 3 (priority 0): icons above the glass. During a press/switch only the target
            // (or, while pressing, none) remains here.
            renderIcons(priorityZero)
        }
    }
}

@Composable
private fun NowPlayingPage(
    snapshot: AndroidPlaybackSnapshot,
    lyricLines: List<AndroidTimedLyricLine>,
    lyricsLoading: Boolean,
    lyricsMessage: String?,
    lyricFollowDelayMillis: Long,
    lyricAnimationSpeed: LyricAnimationSpeed,
    wordLyricsEnabled: Boolean,
    lyricGlowEnabled: Boolean,
    liquidGlassEnabled: Boolean,
    lyricFontSizeSp: Int,
    showFullLyrics: Boolean,
    isLiked: Boolean,
    onToggleLiked: () -> Unit,
    onDismiss: () -> Unit,
    onToggle: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSeek: (Long) -> Unit,
    onLyrics: () -> Unit,
    glass: Boolean,
    modifier: Modifier = Modifier,
) {
    val track = snapshot.track ?: return
    val colors = MaterialTheme.colorScheme
    val kashifGlass = rememberLazerLiquidGlass(liquidGlassEnabled, colors.background)
    val glassBackdrop = if (glass) rememberPageBackdrop() else null
    val duration = snapshot.durationMillis.takeIf { it > 0L } ?: track.durationMillis
    val target = if (duration > 0) snapshot.positionMillis.toFloat() / duration else 0f
    val display by animateFloatAsState(
        target.coerceIn(0f, 1f),
        if (snapshot.isPlaying) spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh) else snap(),
        label = "android-playback-progress",
    )
    var seeking by remember(track.id) { mutableStateOf(false) }
    var seekProgress by remember(track.id) { mutableFloatStateOf(display) }
    LaunchedEffect(display, seeking) { if (!seeking) seekProgress = display }

    // Keep the visual background edge-to-edge; only the controls need to avoid system bars.
    Surface(modifier.fillMaxSize(), color = if (glass) Color.Transparent else colors.background) {
        Box(Modifier.fillMaxSize()) {
            if (glass) {
                GlassBackground(glassBackdrop!!)
            }
        if (isLandscapeLayout()) {
            Box(Modifier.fillMaxSize()) {
                if (!glass) AndroidAlbumFlowBackground(
                    track = track,
                    modifier = Modifier
                        .fillMaxSize()
                        .captureLiquidGlass(kashifGlass),
                    cornerRadius = 0.dp,
                    veil = colors.background.copy(alpha = 0.38f),
                )
                Row(
                    Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(horizontal = 36.dp, vertical = 0.dp),
                ) {
                    Column(
                        Modifier
                            .widthIn(min = 230.dp, max = 320.dp)
                            .fillMaxSize()
                            .padding(10.dp)
                            .liquidGlassSurface(kashifGlass, RoundedCornerShape(24.dp), colors.surface)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, tr("player.collapse"), tint = colors.onSurfaceVariant) }
                            Text(tr("player.now_playing"), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                        }
                        MobileArtwork(
                            track.coverUrl,
                            track.title,
                            Modifier.size(132.dp).align(Alignment.CenterHorizontally),
                            20.dp,
                            onClick = onLyrics,
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(track.title, color = colors.onBackground, style = MaterialTheme.typography.titleLarge, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(track.displayArtist, color = colors.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Spacer(Modifier.weight(1f))
                        if (glassBackdrop != null) {
                            LiquidSlider(
                                value = { seekProgress },
                                onValueChange = { seeking = true; seekProgress = it },
                                valueRange = 0f..1f,
                                visibilityThreshold = 0.0005f,
                                backdrop = glassBackdrop,
                                modifier = Modifier.fillMaxWidth(),
                                onValueChangeFinished = { seeking = false; onSeek((duration * seekProgress).toLong()) },
                            )
                        } else {
                            ThinSeekBar(
                                progress = seekProgress,
                                bufferedProgress = snapshot.bufferedFraction,
                                onSeek = { seeking = true; seekProgress = it },
                                onFinished = { seeking = false; onSeek((duration * seekProgress).toLong()) },
                            )
                        }
                        Row(Modifier.fillMaxWidth()) {
                            Text(formatPlaybackTime((duration * seekProgress).toLong()), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                            Spacer(Modifier.weight(1f))
                            Text(formatPlaybackTime(duration), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            if (glassBackdrop != null) {
                                val palette = LocalGlassPalette.current
                                GlassIconButton(48.dp, glassBackdrop, Icons.Filled.SkipPrevious, tr("player.previous"), onPrevious, iconSize = 28.dp)
                                Box(
                                    Modifier
                                        .size(64.dp)
                                        .glassTile(glassBackdrop, cornerRadius = 32.dp, tint = palette.Accent.copy(alpha = 0.55f))
                                        .clickable(role = Role.Button, onClick = onToggle),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                        if (snapshot.isPlaying) tr("player.pause") else tr("player.play"),
                                        Modifier.size(30.dp),
                                        tint = Color(0xFF04211D),
                                    )
                                }
                                GlassIconButton(48.dp, glassBackdrop, Icons.Filled.SkipNext, tr("player.next"), onNext, iconSize = 28.dp)
                                GlassIconButton(
                                    48.dp, glassBackdrop,
                                    if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    if (isLiked) tr("player.like.remove") else tr("player.like.add"),
                                    onToggleLiked,
                                    tint = if (isLiked) palette.Accent else palette.InkHi,
                                )
                            } else {
                                IconButton(onClick = onPrevious, modifier = Modifier.size(48.dp)) { Icon(Icons.Filled.SkipPrevious, tr("player.previous"), Modifier.size(30.dp), tint = colors.onBackground) }
                                IconButton(onClick = onToggle, modifier = Modifier.size(60.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                                    Icon(if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (snapshot.isPlaying) tr("player.pause") else tr("player.play"), Modifier.size(32.dp))
                                }
                                IconButton(onClick = onNext, modifier = Modifier.size(48.dp)) { Icon(Icons.Filled.SkipNext, tr("player.next"), Modifier.size(30.dp), tint = colors.onBackground) }
                                IconButton(onClick = onToggleLiked, modifier = Modifier.size(48.dp)) {
                                    Icon(if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, if (isLiked) tr("player.like.remove") else tr("player.like.add"), tint = if (isLiked) colors.primary else colors.onBackground)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.width(24.dp))
                    AndroidLyricsViewport(
                        track = track,
                        lines = lyricLines,
                        isLoading = lyricsLoading,
                        message = lyricsMessage,
                        positionMillis = snapshot.positionMillis,
                        followDelayMillis = lyricFollowDelayMillis,
                        animationSpeed = lyricAnimationSpeed,
                        wordLyricsEnabled = wordLyricsEnabled,
                        lyricGlowEnabled = lyricGlowEnabled,
                        lyricFontSizeSp = lyricFontSizeSp,
                        showFullLyrics = showFullLyrics,
                        onSeek = onSeek,
                        modifier = Modifier.weight(1f).fillMaxSize(),
                    )
                }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, tr("player.collapse"), tint = colors.onSurfaceVariant) }
                Text(tr("player.now_playing"), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
            }
            // The artwork absorbs the leftover height so the controls below never fall off
            // shorter screens (e.g. 1080x2000 emulator windows).
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val artworkSide = minOf(maxWidth, maxHeight)
                MobileArtwork(
                    track.coverUrl,
                    track.title,
                    Modifier.size(artworkSide),
                    30.dp,
                    onClick = onLyrics,
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(track.title, style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.displayArtist, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (glassBackdrop != null) {
                    val palette = LocalGlassPalette.current
                    GlassIconButton(
                        42.dp, glassBackdrop,
                        if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        if (isLiked) tr("player.like.remove") else tr("player.like.add"),
                        onToggleLiked,
                        tint = if (isLiked) palette.Accent else palette.InkHi,
                    )
                } else {
                    IconButton(onClick = onToggleLiked, modifier = Modifier.size(48.dp)) {
                        Icon(if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder, if (isLiked) tr("player.like.remove") else tr("player.like.add"), tint = if (isLiked) colors.primary else colors.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(28.dp))
            if (glassBackdrop != null) {
                LiquidSlider(
                    value = { seekProgress },
                    onValueChange = { seeking = true; seekProgress = it },
                    valueRange = 0f..1f,
                    visibilityThreshold = 0.0005f,
                    backdrop = glassBackdrop,
                    modifier = Modifier.fillMaxWidth(),
                    onValueChangeFinished = { seeking = false; onSeek((duration * seekProgress).toLong()) },
                )
            } else {
                ThinSeekBar(
                    progress = seekProgress,
                    bufferedProgress = snapshot.bufferedFraction,
                    onSeek = { seeking = true; seekProgress = it },
                    onFinished = { seeking = false; onSeek((duration * seekProgress).toLong()) },
                )
            }
            Row(Modifier.fillMaxWidth()) {
                Text(formatPlaybackTime((duration * seekProgress).toLong()), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(formatPlaybackTime(duration), style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                if (glassBackdrop != null) {
                    val palette = LocalGlassPalette.current
                    GlassIconButton(52.dp, glassBackdrop, Icons.Filled.SkipPrevious, tr("player.previous"), onPrevious, iconSize = 28.dp)
                    Box(
                        Modifier
                            .size(68.dp)
                            .glassTile(glassBackdrop, cornerRadius = 34.dp, tint = palette.Accent.copy(alpha = 0.55f))
                            .clickable(role = Role.Button, onClick = onToggle),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                            if (snapshot.isPlaying) tr("player.pause") else tr("player.play"),
                            Modifier.size(32.dp),
                            tint = Color(0xFF04211D),
                        )
                    }
                    GlassIconButton(52.dp, glassBackdrop, Icons.Filled.SkipNext, tr("player.next"), onNext, iconSize = 28.dp)
                } else {
                    IconButton(onClick = onPrevious, modifier = Modifier.size(50.dp)) { Icon(Icons.Filled.SkipPrevious, tr("player.previous"), Modifier.size(31.dp)) }
                    IconButton(onClick = onToggle, modifier = Modifier.size(68.dp), colors = IconButtonDefaults.iconButtonColors(containerColor = colors.primary, contentColor = colors.onPrimary)) {
                        Icon(if (snapshot.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, if (snapshot.isPlaying) tr("player.pause") else tr("player.play"), Modifier.size(35.dp))
                    }
                    IconButton(onClick = onNext, modifier = Modifier.size(50.dp)) { Icon(Icons.Filled.SkipNext, tr("player.next"), Modifier.size(31.dp)) }
                }
            }
            Spacer(Modifier.weight(1f))
            if (glassBackdrop != null) {
                LiquidButton(
                    onClick = onLyrics,
                    backdrop = glassBackdrop,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                ) {
                    Icon(Icons.Outlined.Lyrics, null, Modifier.size(18.dp), tint = LocalGlassPalette.current.InkHi)
                    Spacer(Modifier.width(7.dp))
                    Text(tr("player.lyrics"), color = LocalGlassPalette.current.InkHi)
                }
            } else {
                ThemeTextButton(onClick = onLyrics, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Icon(Icons.Outlined.Lyrics, null, Modifier.size(18.dp)); Spacer(Modifier.width(7.dp)); Text(tr("player.lyrics"))
                }
            }
            snapshot.message?.let { Text(it, Modifier.fillMaxWidth().padding(bottom = 8.dp), style = MaterialTheme.typography.bodySmall, color = colors.error, textAlign = TextAlign.Center) }
            }
        }
        }
    }
}

/** Same visual construction as the desktop control: base rail, buffered rail, then blue playhead. */
@Composable
private fun ThinSeekBar(
    progress: Float,
    bufferedProgress: Float,
    onSeek: (Float) -> Unit,
    onFinished: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val fraction = progress.coerceIn(0f, 1f)
    val buffered = maxOf(fraction, bufferedProgress.coerceIn(0f, 1f))
    BoxWithConstraints(
        Modifier.height(20.dp).fillMaxWidth().pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                val width = size.width.coerceAtLeast(1)
                onSeek((down.position.x / width).coerceIn(0f, 1f)); down.consume()
                while (true) {
                    val change = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                    onSeek((change.position.x / width).coerceIn(0f, 1f)); change.consume()
                    if (!change.pressed) break
                }
                onFinished()
            }
        },
        contentAlignment = Alignment.CenterStart,
    ) {
        val travel = (maxWidth - 10.dp).coerceAtLeast(0.dp)
        Box(Modifier.fillMaxWidth().height(3.dp).clip(CircleShape).background(colors.surfaceVariant.copy(alpha = 0.95f)))
        Box(Modifier.fillMaxWidth(buffered).height(3.dp).clip(CircleShape).background(colors.onSurfaceVariant.copy(alpha = 0.34f)))
        Box(Modifier.fillMaxWidth(fraction).height(3.dp).clip(CircleShape).background(colors.primary))
        Box(Modifier.padding(start = travel * fraction).size(10.dp).clip(CircleShape).background(colors.primary))
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun ImportOptionsSheet(
    onSelectFiles: () -> Unit,
    onSelectWifi: () -> Unit,
    onSelectWebdav: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val sheetBackdrop = kyantRememberLayerBackdrop()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .background(colors.surface)
                    .kyantLayerBackdrop(sheetBackdrop),
            )
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 36.dp),
            ) {
                Text(tr("internal.import"), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(
                    tr("internal.empty"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))

                ImportOptionRow(
                    imageVector = Icons.Outlined.FolderOpen,
                    title = tr("internal.files"),
                    desc = "选择本地音频文件与同名 .lrc 歌词一并导入",
                    onClick = {
                        onDismiss()
                        onSelectFiles()
                    },
                )
                Spacer(Modifier.height(10.dp))

                ImportOptionRow(
                    imageVector = Icons.Outlined.Wifi,
                    title = tr("internal.wifi"),
                    desc = tr("wifi.hint"),
                    onClick = {
                        onDismiss()
                        onSelectWifi()
                    },
                )
                Spacer(Modifier.height(10.dp))

                ImportOptionRow(
                    imageVector = Icons.Outlined.CloudDownload,
                    title = tr("internal.webdav"),
                    desc = "连接坚果云、Alist 或 Nextcloud 网盘下载",
                    onClick = {
                        onDismiss()
                        onSelectWebdav()
                    },
                )
            }
        }
    }
}

@Composable
private fun ImportOptionRow(
    imageVector: ImageVector,
    title: String,
    desc: String,
    onClick: () -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.surface.luminance() < 0.5f
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "import_option_row_scale",
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color(0xFF242C34) else Color(0xFFF4F7FA),
        border = BorderStroke(1.dp, if (isDark) Color(0xFF384452) else Color(0xFFE2E7ED)),
        interactionSource = interaction,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.primary.copy(alpha = if (isDark) 0.22f else 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text(desc, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun WifiImportSheet(controller: AndroidLibraryController, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
    var copiedUrl by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        // Sheet-local flat backdrop so LiquidButton glass has something to sample without
        // referencing a layer that contains the button itself.
        val sheetBackdrop = kyantRememberLayerBackdrop()
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .background(colors.surface)
                    .kyantLayerBackdrop(sheetBackdrop),
            )
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 28.dp)) {
                Text(tr("wifi.title"), style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(6.dp))
                Text(
                    tr("wifi.hint"),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant,
                )
                Spacer(Modifier.height(20.dp))
                if (controller.wifiServerRunning) {
                    Text(tr("wifi.running"), style = MaterialTheme.typography.titleSmall, color = colors.primary)
                    Spacer(Modifier.height(12.dp))

                    val hasEmulator = controller.wifiAddresses.any { it == "127.0.0.1" || it.startsWith("10.0.2.") }

                    controller.wifiAddresses.forEach { address ->
                        val url = "http://$address:${WifiTransferServer.PORT}"
                        Surface(
                            onClick = {
                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(url))
                                copiedUrl = url
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = colors.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        ) {
                            Row(
                                Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    url,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.onSurface,
                                )
                                Text(
                                    if (copiedUrl == url) tr("wifi.copied") else "复制",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = colors.primary,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    if (controller.wifiAddresses.isEmpty()) {
                        Text(
                            tr("wifi.no_network"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.error,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }

                    if (hasEmulator) {
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = colors.primaryContainer.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(
                                tr("wifi.emulator_tip"),
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))
                    GlassActionButton(tr("wifi.stop"), controller::stopWifiServer, sheetBackdrop, accent = true)
                } else {
                    GlassActionButton(tr("wifi.start"), controller::startWifiServer, sheetBackdrop, accent = true)
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun WebDavSheet(controller: AndroidLibraryController, onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var urlDraft by remember { mutableStateOf(controller.webDavSavedUrl) }
    var userDraft by remember { mutableStateOf(controller.webDavSavedUser) }
    var passDraft by remember { mutableStateOf(controller.webDavSavedPass) }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = colors.surface,
    ) {
        val sheetBackdrop = kyantRememberLayerBackdrop()
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .background(colors.surface)
                    .kyantLayerBackdrop(sheetBackdrop),
            )
            Column(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(tr("webdav.title"), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(
                    value = urlDraft,
                    onValueChange = { urlDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr("webdav.url")) },
                    placeholder = { Text("https://dav.jianguoyun.com/dav/") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = userDraft,
                    onValueChange = { userDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr("webdav.user")) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                )
                OutlinedTextField(
                    value = passDraft,
                    onValueChange = { passDraft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(tr("webdav.pass")) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                    GlassActionButton(tr("common.cancel"), onDismiss, sheetBackdrop)
                    Spacer(Modifier.width(8.dp))
                    GlassActionButton(
                        tr("webdav.connect"),
                        { controller.connectWebDav(urlDraft, userDraft, passDraft, "") },
                        sheetBackdrop,
                        enabled = urlDraft.isNotBlank(),
                        accent = true,
                    )
                }
                when {
                    controller.webdavLoading -> QuietState(tr("webdav.loading"))
                    controller.webdavEntries.isNotEmpty() -> Column {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (controller.webdavPath.isNotEmpty()) {
                                GlassActionButton(
                                    tr("webdav.parent"),
                                    { controller.browseWebDav(webDavParent(controller.webdavPath)) },
                                    sheetBackdrop,
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                "/" + controller.webdavPath,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.onSurfaceVariant,
                            )
                        }
                        controller.webdavEntries
                            .filter { it.isFolder || InternalMusicLibrary.isAudioFile(it.name) || it.name.lowercase().endsWith(".lrc") }
                            .forEach { entry ->
                                Row(
                                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            if (entry.isFolder) entry.name + "/" else entry.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        if (!entry.isFolder && entry.sizeBytes > 0) {
                                            Text(
                                                "%.1f MB".format(entry.sizeBytes / 1048576.0),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    when {
                                        entry.isFolder -> GlassActionButton(
                                            tr("webdav.open"),
                                            { controller.browseWebDav(webDavChild(controller.webdavPath, entry.name)) },
                                            sheetBackdrop,
                                        )
                                        controller.webdavDownloading == entry.name ->
                                            Text(
                                                tr("webdav.downloading", entry.name),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = colors.primary,
                                            )
                                        else -> GlassActionButton(
                                            tr("webdav.download"),
                                            { controller.downloadWebDav(entry) },
                                            sheetBackdrop,
                                        )
                                    }
                                }
                            }
                    }
                }
            }
        }
    }
}

private fun webDavChild(path: String, name: String): String = if (path.isEmpty()) name else "$path/$name"

private fun webDavParent(path: String): String = path.substringBeforeLast('/', "")

@Composable
private fun QuietState(text: String) {
    Text(text, Modifier.fillMaxWidth().padding(vertical = 14.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
}

@Composable
private fun MessageBanner(text: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 5.dp) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 10.dp), style = MaterialTheme.typography.bodySmall)
    }
}

private fun AndroidRootDestination.icon() = when (this) {
    AndroidRootDestination.LIBRARY -> Icons.Outlined.LibraryMusic
    AndroidRootDestination.SEARCH -> Icons.Outlined.Search
    AndroidRootDestination.SETTINGS -> Icons.Outlined.Settings
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun AndroidLazerPreview() = AndroidLazerApp()
