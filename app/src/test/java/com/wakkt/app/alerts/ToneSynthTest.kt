package com.wakkt.app.alerts

import com.wakkt.app.data.NoteSound
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class ToneSynthTest {
    @Test fun `none is silent and empty`() {
        assertEquals(0, ToneSynth.pcm(NoteSound.NONE).size)
        assertEquals(0L, ToneSynth.durationMs(NoteSound.NONE))
    }

    @Test fun `every tone is audible, short and never clips`() {
        NoteSound.entries.filter { it != NoteSound.NONE }.forEach { sound ->
            val pcm = ToneSynth.pcm(sound)
            val peak = pcm.maxOf { abs(it.toInt()) }
            assertTrue("$sound too quiet: $peak", peak > Short.MAX_VALUE / 8)
            assertTrue("$sound clips", peak < Short.MAX_VALUE)
            assertTrue("$sound too long", ToneSynth.durationMs(sound) in 200..1500)
        }
    }

    @Test fun `tones start and end near silence so they don't click`() {
        NoteSound.entries.filter { it != NoteSound.NONE }.forEach { sound ->
            val pcm = ToneSynth.pcm(sound)
            assertTrue("$sound starts loud", abs(pcm.first().toInt()) < 500)
            assertTrue("$sound ends loud", abs(pcm.last().toInt()) < 2000)
        }
    }
}
