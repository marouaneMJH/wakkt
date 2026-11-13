package com.wakkt.app.overlay

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * Deliberately unpleasant, randomly generated animated visuals: garish clashing colors, crawling
 * stripes, hypnotic rings, rotating rays, drifting blobs. A new scene is rolled every few seconds.
 *
 * Photosensitivity safety (WCAG 2.3.1-style): nothing strobes. Colors drift smoothly, and every
 * repeating pattern alternates at most [MAX_ALTERNATION_HZ] times per second at any point.
 */
@Composable
fun ChaosCanvas(seed: Long, modifier: Modifier = Modifier) {
    var epoch by remember(seed) { mutableIntStateOf(0) }
    LaunchedEffect(seed) {
        val r = Random(seed)
        while (true) {
            delay(r.nextLong(3_500, 6_500))
            epoch++
        }
    }
    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) withFrameNanos { time = (it - start) / 1_000_000_000f }
    }
    Crossfade(targetState = remember(seed, epoch) { ChaosScene.random(Random(seed * 1_000_003 + epoch)) }, animationSpec = tween(700), label = "chaos", modifier = modifier) { scene ->
        Canvas(Modifier.fillMaxSize()) { scene.draw(this, time) }
    }
}

const val MAX_ALTERNATION_HZ = 2.5f

/** Hues are stored, not colors, so the whole scene can drift around the color wheel. */
class ChaosScene private constructor(
    private val hues: List<Float>,
    private val hueDriftDegPerSec: Float,
    private val layers: List<Layer>,
) {
    fun color(i: Int, t: Float, alpha: Float = 1f): Color {
        val h = ((hues[i % hues.size] + t * hueDriftDegPerSec) % 360f + 360f) % 360f
        return Color.hsv(h, 1f, if (i % 2 == 0) 1f else 0.85f, alpha)
    }

    fun draw(scope: DrawScope, t: Float) {
        scope.drawRect(color(0, t))
        layers.forEach { it.draw(scope, this, t) }
    }

    sealed interface Layer {
        fun draw(s: DrawScope, scene: ChaosScene, t: Float)
        /** Fastest full light/dark cycles per second seen at any single point. */
        val cyclesPerSecond: Float
    }

    /** Worst-case alternation rate of the whole scene; must stay under [MAX_ALTERNATION_HZ]. */
    val maxCyclesPerSecond: Float get() = layers.maxOf { it.cyclesPerSecond }
    val layerCount: Int get() = layers.size

    /** Diagonal stripes crawling sideways. */
    private class Stripes(val angle: Float, val widthDp: Float, val c1: Int, val c2: Int, val alpha: Float, val speedDp: Float) : Layer {
        override val cyclesPerSecond get() = speedDp / (2 * widthDp)
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val w = widthDp * density
            val diag = hypot(size.width, size.height)
            val shift = (t * speedDp * density) % (2 * w)
            rotate(angle) {
                var x = -diag - 2 * w + shift
                var i = 0
                while (x < diag) {
                    drawRect(scene.color(if (i % 2 == 0) c1 else c2, t, alpha), Offset(x + center.x - diag / 2, center.y - diag), Size(w + 1, diag * 2))
                    x += w; i++
                }
            }
        }
    }

    /** Concentric rings flowing out of a point. */
    private class Rings(val cx: Float, val cy: Float, val widthDp: Float, val c1: Int, val c2: Int, val alpha: Float, val speedDp: Float) : Layer {
        override val cyclesPerSecond get() = speedDp / (2 * widthDp)
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val w = widthDp * density
            val c = Offset(size.width * cx, size.height * cy)
            val maxR = hypot(max(c.x, size.width - c.x), max(c.y, size.height - c.y)) + 2 * w
            val shift = (t * speedDp * density) % (2 * w)
            var r = maxR + shift
            var i = 0
            while (r > 0) {
                drawCircle(scene.color(if (i % 2 == 0) c1 else c2, t, alpha), r, c)
                r -= w; i++
            }
        }
    }

    /** Sunburst rays rotating around a point. */
    private class Rays(val cx: Float, val cy: Float, val count: Int, val c1: Int, val alpha: Float, val degPerSec: Float) : Layer {
        override val cyclesPerSecond get() = count * kotlin.math.abs(degPerSec) / 360f
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val c = Offset(size.width * cx, size.height * cy)
            val r = hypot(size.width, size.height) * 2
            val step = 2 * PI / count
            val base = (t * degPerSec) * PI / 180
            val col = scene.color(c1, t, alpha)
            for (k in 0 until count) {
                val a0 = base + k * step
                val a1 = a0 + step / 2
                val p = Path().apply {
                    moveTo(c.x, c.y)
                    lineTo(c.x + (r * cos(a0)).toFloat(), c.y + (r * sin(a0)).toFloat())
                    lineTo(c.x + (r * cos(a1)).toFloat(), c.y + (r * sin(a1)).toFloat())
                    close()
                }
                drawPath(p, col)
            }
        }
    }

    /** Warping checkerboard. */
    private class Checker(val cellDp: Float, val c1: Int, val alpha: Float, val wobbleDp: Float) : Layer {
        // Peak sideways speed is wobble * 0.9 dp/s; one cycle is two cells.
        override val cyclesPerSecond get() = wobbleDp * 0.9f / (2 * cellDp)
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val cell = cellDp * density
            val col = scene.color(c1, t, alpha)
            var row = 0
            var y = -cell
            while (y < size.height + cell) {
                val dx = sin(t * 0.9f + row * 0.6f) * wobbleDp * density
                var x = -cell * 2 + dx + if (row % 2 == 0) 0f else cell
                while (x < size.width + cell) {
                    drawRect(col, Offset(x, y), Size(cell, cell))
                    x += cell * 2
                }
                y += cell; row++
            }
        }
    }

    /** Big translucent blobs drifting on Lissajous paths. */
    private class Blobs(val blobs: List<FloatArray>, val alpha: Float) : Layer {
        // Soft drifting shapes; at most one pass per oscillation.
        override val cyclesPerSecond get() = blobs.maxOf { max(it[2], it[3]) } / (2 * PI.toFloat())
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val m = size.minDimension
            blobs.forEachIndexed { i, b -> // b = [ax, ay, fx, fy, phase, radius]
                val x = size.width * (0.5f + b[0] * sin(t * b[2] + b[4]))
                val y = size.height * (0.5f + b[1] * cos(t * b[3] + b[4]))
                drawCircle(scene.color(i + 1, t, alpha), m * b[5], Offset(x, y))
            }
        }
    }

    /** Thick zigzag bands crawling down. */
    private class Zigzag(val bandDp: Float, val c1: Int, val alpha: Float, val speedDp: Float) : Layer {
        override val cyclesPerSecond get() = speedDp / (2 * bandDp)
        override fun draw(s: DrawScope, scene: ChaosScene, t: Float) = with(s) {
            val band = bandDp * density
            val half = band * 0.9f
            val amp = band * 0.45f
            val col = scene.color(c1, t, alpha)
            var y = -2 * band + (t * speedDp * density) % (2 * band)
            clipRect {
                while (y < size.height + band) {
                    val p = Path().apply {
                        moveTo(-half, y)
                        var x = -half
                        var up = true
                        while (x < size.width + half) { x += half; lineTo(x, if (up) y - amp else y + amp); up = !up }
                    }
                    drawPath(p, col, style = Stroke(band * 0.5f))
                    y += 2 * band
                }
            }
        }
    }

    companion object {
        fun random(r: Random): ChaosScene {
            // Clashing palette: hues spread roughly opposite each other, fully saturated.
            val h0 = r.nextFloat() * 360f
            val hues = listOf(h0, h0 + 150f + r.nextFloat() * 30f, h0 + 60f + r.nextFloat() * 40f, h0 + 220f + r.nextFloat() * 40f)
            val drift = (if (r.nextBoolean()) 1 else -1) * (15f + r.nextFloat() * 35f)
            val count = if (r.nextFloat() < 0.6f) 2 else 3
            val layers = List(count) { i -> randomLayer(r, if (i == 0) 1f else 0.45f + r.nextFloat() * 0.35f) }
            return ChaosScene(hues, drift, layers)
        }

        /** Speed for a pattern of period [periodDp] that keeps alternation under the safety limit. */
        private fun safeSpeed(r: Random, periodDp: Float): Float =
            periodDp * MAX_ALTERNATION_HZ * (0.25f + r.nextFloat() * 0.75f)

        private fun randomLayer(r: Random, alpha: Float): Layer {
            val c1 = 1 + r.nextInt(3)
            val c2 = (c1 + 1 + r.nextInt(2)) % 4
            return when (r.nextInt(6)) {
                0 -> { val w = 10f + r.nextFloat() * 22f; Stripes(r.nextFloat() * 180f, w, c1, c2, alpha, safeSpeed(r, 2 * w)) }
                1 -> { val w = 12f + r.nextFloat() * 20f; Rings(r.nextFloat(), r.nextFloat(), w, c1, c2, alpha, safeSpeed(r, 2 * w)) }
                2 -> {
                    val n = 8 + r.nextInt(10)
                    // A point is crossed by n rays per revolution: keep n * rev/s under the limit.
                    val maxDegPerSec = 360f * MAX_ALTERNATION_HZ / n
                    Rays(r.nextFloat(), r.nextFloat(), n, c1, alpha, (if (r.nextBoolean()) 1 else -1) * maxDegPerSec * (0.3f + r.nextFloat() * 0.7f))
                }
                3 -> Checker(14f + r.nextFloat() * 22f, c1, alpha, 6f + r.nextFloat() * 14f)
                4 -> Blobs(List(4 + r.nextInt(4)) {
                    floatArrayOf(0.2f + r.nextFloat() * 0.35f, 0.2f + r.nextFloat() * 0.35f, 0.3f + r.nextFloat() * 0.9f, 0.3f + r.nextFloat() * 0.9f, r.nextFloat() * 6f, 0.15f + r.nextFloat() * 0.3f)
                }, 0.55f + r.nextFloat() * 0.3f)
                else -> { val b = 14f + r.nextFloat() * 18f; Zigzag(b, c1, alpha, safeSpeed(r, 2 * b)) }
            }
        }
    }
}
