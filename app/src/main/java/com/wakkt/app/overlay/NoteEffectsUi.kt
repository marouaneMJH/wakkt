package com.wakkt.app.overlay

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.wakkt.app.data.NoteMotion
import com.wakkt.app.data.NotePattern
import kotlinx.coroutines.delay
import kotlin.random.Random

private val ZERO = object : State<Float> { override val value = 0f }
private val ONE = object : State<Float> { override val value = 1f }

/** Animated transform values for a [NoteMotion]. Read them in a graphicsLayer lambda. */
@Stable
class MotionValues(val scale: State<Float>, val rotation: State<Float>, val translationY: State<Float>, val flash: State<Float>)

@Composable
fun rememberMotion(motion: NoteMotion): MotionValues {
    if (motion == NoteMotion.NONE) return remember { MotionValues(ONE, ZERO, ZERO, ZERO) }
    val t = rememberInfiniteTransition(label = "noteMotion")
    val bouncePx = with(LocalDensity.current) { 10.dp.toPx() }
    return when (motion) {
        NoteMotion.PULSE -> MotionValues(
            t.animateFloat(1f, 1.045f, infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse), "pulse"),
            ZERO, ZERO, ZERO,
        )
        NoteMotion.SHAKE -> MotionValues(
            ONE,
            t.animateFloat(
                0f, 0f,
                infiniteRepeatable(keyframes {
                    durationMillis = 2000
                    0f at 0; -4f at 70; 4f at 140; -3.5f at 210; 3f at 280; -1.5f at 350; 0f at 420; 0f at 2000
                }),
                "shake",
            ),
            ZERO, ZERO,
        )
        NoteMotion.BOUNCE -> MotionValues(
            ONE, ZERO,
            t.animateFloat(
                0f, 0f,
                infiniteRepeatable(keyframes {
                    durationMillis = 1600
                    0f at 0; -bouncePx at 220 using FastOutSlowInEasing; 0f at 440
                    -bouncePx * 0.4f at 580; 0f at 720; 0f at 1600
                }),
                "bounce",
            ),
            ZERO,
        )
        NoteMotion.FLASH -> MotionValues(
            ONE, ZERO, ZERO,
            t.animateFloat(0.1f, 1f, infiniteRepeatable(tween(450), RepeatMode.Reverse), "flash"),
        )
        NoteMotion.NONE -> error("handled above")
    }
}

/** Draws [pattern] over the whole draw area in [color] (use a faint ink color). */
fun DrawScope.drawPattern(pattern: NotePattern, color: Color) {
    val u = 1.dp.toPx()
    when (pattern) {
        NotePattern.NONE -> Unit
        NotePattern.STRIPES -> {
            val step = 14 * u
            var x = -size.height
            while (x < size.width) {
                drawLine(color, Offset(x, size.height), Offset(x + size.height, 0f), 3 * u)
                x += step
            }
        }
        NotePattern.DOTS -> {
            val step = 12 * u
            var y = step / 2
            var row = 0
            while (y < size.height) {
                var x = if (row % 2 == 0) step / 2 else step
                while (x < size.width) { drawCircle(color, 1.6f * u, Offset(x, y)); x += step }
                y += step; row++
            }
        }
        NotePattern.GRID -> {
            val step = 16 * u
            var x = step
            while (x < size.width) { drawLine(color, Offset(x, 0f), Offset(x, size.height), u); x += step }
            var y = step
            while (y < size.height) { drawLine(color, Offset(0f, y), Offset(size.width, y), u); y += step }
        }
        NotePattern.ZIGZAG -> {
            val rowStep = 14 * u
            val half = 6 * u
            val amp = 3.5f * u
            var y = rowStep / 2
            while (y < size.height + amp) {
                val path = Path().apply {
                    moveTo(0f, y)
                    var x = 0f
                    var up = true
                    while (x < size.width) { x += half; lineTo(x, if (up) y - amp else y + amp); up = !up }
                }
                drawPath(path, color, style = Stroke(1.5f * u))
                y += rowStep
            }
        }
    }
}

/** Animated TV static. Place it over content with Modifier.matchParentSize(). */
@Composable
fun TvStaticLayer(alpha: Float, modifier: Modifier = Modifier) {
    val brushes = remember { NoiseTextures.frames.map { ShaderBrush(ImageShader(it, TileMode.Repeated, TileMode.Repeated)) } }
    var frame by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) { delay(70); frame = (frame + 1) % brushes.size }
    }
    Box(modifier.drawBehind { drawRect(brushes[frame], alpha = alpha) })
}

private object NoiseTextures {
    val frames: List<ImageBitmap> by lazy {
        val size = 96
        val rnd = Random(7)
        List(4) {
            val px = IntArray(size * size) {
                val v = rnd.nextInt(256)
                (0xFF shl 24) or (v shl 16) or (v shl 8) or v
            }
            Bitmap.createBitmap(px, size, size, Bitmap.Config.ARGB_8888).asImageBitmap()
        }
    }
}

/** Random glitch bursts: a horizontal jitter plus colored slice artifacts. */
@Stable
class GlitchState {
    var offsetX by mutableFloatStateOf(0f)
    /** 0 = no artifacts this frame; otherwise a seed for the slice positions. */
    var seed by mutableIntStateOf(0)
}

@Composable
fun rememberGlitch(enabled: Boolean): GlitchState {
    val state = remember { GlitchState() }
    val jitter = with(LocalDensity.current) { 6.dp.toPx() }
    LaunchedEffect(enabled) {
        state.offsetX = 0f; state.seed = 0
        if (!enabled) return@LaunchedEffect
        while (true) {
            delay(Random.nextLong(1600, 3200))
            repeat(Random.nextInt(4, 8)) {
                state.offsetX = Random.nextFloat() * 2 * jitter - jitter
                state.seed = Random.nextInt(1, Int.MAX_VALUE)
                delay(45)
            }
            state.offsetX = 0f; state.seed = 0
        }
    }
    return state
}

fun DrawScope.drawGlitch(seed: Int) {
    if (seed == 0) return
    val rnd = Random(seed)
    val u = 1.dp.toPx()
    repeat(3) {
        val y = rnd.nextFloat() * size.height
        val h = (2 + rnd.nextFloat() * 8) * u
        val shift = (3 + rnd.nextFloat() * 6) * u
        drawRect(Color(0x6600FFFF), Offset(shift, y), Size(size.width, h))
        drawRect(Color(0x66FF00FF), Offset(-shift, y + h * 0.5f), Size(size.width, h * 0.7f))
    }
}
