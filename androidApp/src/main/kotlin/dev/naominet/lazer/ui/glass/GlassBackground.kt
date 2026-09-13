package dev.naominet.lazer.ui.glass

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import dev.naominet.lazer.ui.theme.LocalGlassPalette
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

/** 页面背景 backdrop：所有玻璃层都从这一层取样。 */
@Composable
fun rememberPageBackdrop(): LayerBackdrop = rememberLayerBackdrop()

/**
 * 纯白舞台 —— 玻璃皮肤的身份色就是纯白：无论系统明暗，背景恒为 #FFFFFF，
 * 质感完全由玻璃卡片自身的磨砂、折射、45° 高光与投影表达。
 * [imagePath] 非空时仍支持自定义图片铺底（当前设置页未提供入口，保留能力）。
 */
@SuppressLint("NewApi")
@Composable
fun GlassBackground(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
    imagePath: String? = null, auroraColorA: Color? = null, auroraColorB: Color? = null
) {
    val p = LocalGlassPalette.current
    val imageBitmap = rememberDecodedImage(imagePath)

    Box(modifier.fillMaxSize().layerBackdrop(backdrop)) {
        Canvas(Modifier.fillMaxSize()) {
            if (imageBitmap != null) {
                drawImageCover(imageBitmap)
                drawRect(p.VeilImage)
                drawRect(
                    brush = Brush.radialGradient(
                        colorStops = arrayOf(
                            0.30f to Color.Transparent,
                            1f to p.VeilEdge
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.35f),
                        radius = maxOf(size.width, size.height) * 0.78f
                    )
                )
            } else {
                drawRect(Color.White)
            }
        }
    }
}

/**
 * 解码自定义背景图并缓存为 ImageBitmap。
 * 在 IO 线程按 inSampleSize 降采样（最长边 ≤ 1280px），避免大图占用过多内存。
 */
@Composable
private fun rememberDecodedImage(path: String?): ImageBitmap? {
    return produceState<ImageBitmap?>(initialValue = null, path) {
        value = withContext(Dispatchers.IO) { decodeImage(path) }
    }.value
}

private fun decodeImage(path: String?): ImageBitmap? {
    if (path == null) return null
    val file = File(path)
    if (!file.exists()) return null
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        val bigger = maxOf(bounds.outWidth, bounds.outHeight)
        while (bigger / (sample * 2) >= 1280) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = BitmapFactory.decodeFile(path, opts) ?: return null
        bmp.asImageBitmap()
    } catch (_: Exception) {
        null
    }
}

/** 以 cover 方式把图片铺满画布（等比缩放、居中裁剪）。 */
private fun DrawScope.drawImageCover(bitmap: ImageBitmap) {
    val imgW = bitmap.width.toFloat()
    val imgH = bitmap.height.toFloat()
    if (imgW <= 0f || imgH <= 0f) return
    val scale = maxOf(size.width / imgW, size.height / imgH)
    val dw = (imgW * scale).roundToInt()
    val dh = (imgH * scale).roundToInt()
    val dx = ((size.width - dw) / 2f).roundToInt()
    val dy = ((size.height - dh) / 2f).roundToInt()
    drawImage(
        image = bitmap,
        dstOffset = IntOffset(dx, dy),
        dstSize = IntSize(dw, dh)
    )
}

/** 便捷：把整块背景铺满并作为内容容器。 */
@Composable
fun GlassScaffold(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
    imagePath: String? = null, auroraColorA: Color? = null, auroraColorB: Color? = null,
    content: @Composable () -> Unit
) {
    Box(modifier.fillMaxSize()) {
        GlassBackground(backdrop, imagePath = imagePath)
        content()
    }
}
