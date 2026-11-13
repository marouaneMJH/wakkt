package com.wakkt.app.alerts

import com.wakkt.app.data.NoteSound
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/** Synthesizes the alert tones as 16-bit mono PCM, so no audio files are shipped. Pure; unit-tested. */
object ToneSynth {
    const val SAMPLE_RATE = 44_100

    fun pcm(sound: NoteSound): ShortArray = when (sound) {
        NoteSound.NONE -> ShortArray(0)
        // Two bell-like notes (E6 then B6) with exponential decay.
        NoteSound.CHIME -> render(0.9) { t ->
            bell(t, 1318.5) + bell(t - 0.16, 1975.5)
        }
        // Two short square-ish beeps.
        NoteSound.BEEP -> render(0.36) { t ->
            val on = t < 0.12 || (t in 0.2..0.32)
            if (on) 0.45 * soft(sin(2 * PI * 1000 * t)) * edge(if (t < 0.12) t else t - 0.2, 0.12) else 0.0
        }
        // Low rough buzz.
        NoteSound.BUZZ -> render(0.45) { t ->
            val w = sin(2 * PI * 150 * t) + 0.5 * sin(2 * PI * 300 * t) + 0.33 * sin(2 * PI * 450 * t)
            0.4 * soft(w * 2) * edge(t, 0.45)
        }
        // Siren alternating two pitches every 150 ms.
        NoteSound.ALARM -> render(1.2) { t ->
            val f = if ((t / 0.15).toInt() % 2 == 0) 950.0 else 1350.0
            0.4 * soft(sin(2 * PI * f * t) * 1.5) * edge(t, 1.2)
        }
    }

    /** Duration of [sound] in milliseconds. */
    fun durationMs(sound: NoteSound): Long = pcm(sound).size * 1000L / SAMPLE_RATE

    private fun bell(t: Double, f: Double): Double {
        if (t < 0) return 0.0
        val env = exp(-t * 6.0) * min(1.0, t / 0.004)
        return 0.35 * env * (sin(2 * PI * f * t) + 0.3 * sin(2 * PI * f * 2.01 * t))
    }

    /** Soft clipper: keeps rich waveforms from clipping harshly. */
    private fun soft(x: Double) = x / (1 + kotlin.math.abs(x))

    /** 5 ms fade in/out so tones don't click. */
    private fun edge(t: Double, length: Double): Double {
        val f = 0.005
        return min(1.0, min(t / f, (length - t) / f)).coerceIn(0.0, 1.0)
    }

    private inline fun render(seconds: Double, wave: (Double) -> Double): ShortArray {
        val n = (seconds * SAMPLE_RATE).toInt()
        return ShortArray(n) { i ->
            val v = wave(i.toDouble() / SAMPLE_RATE).coerceIn(-1.0, 1.0)
            (v * Short.MAX_VALUE).toInt().toShort()
        }
    }
}
