package com.wakkt.app.ui.editor

import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRules
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteVibration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteIntensityTest {
    @Test fun everyPresetDetectsItself() {
        for (p in NoteIntensity.entries) assertEquals(p, NoteIntensity.detect(p.settings))
    }

    @Test fun presetsAreDistinct() {
        val normalized = NoteIntensity.entries.map { it.settings.normalized() }
        assertEquals(normalized.size, normalized.toSet().size)
    }

    @Test fun presetsUseValuesTheEditorOffers() {
        for (p in NoteIntensity.entries) {
            val s = p.applyTo(NoteIntensity.QUIET.settings)
            assertTrue(p.name, LabelRules.isValidPeriod(s.snoozeMinutes))
            assertTrue(p.name, s.repeatSeconds in LabelEntity.REPEAT_PRESETS)
        }
    }

    @Test fun defaultIsGentle() = assertEquals(NoteIntensity.GENTLE, NoteIntensity.DEFAULT)

    @Test fun exactPresetValues() {
        with(NoteIntensity.GENTLE.settings) {
            assertEquals(LabelBehavior.CLOSABLE_PERIOD, behavior)
            assertEquals(15, snoozeMinutes)
            assertEquals(Backdrop.NONE, backdrop)
            assertEquals(NoteSound.CHIME, sound)
        }
        with(NoteIntensity.FIRM.settings) {
            assertEquals(Backdrop.STRONG, backdrop)
            assertTrue(allowContinue)
            assertEquals(NoteSound.BEEP, sound)
            assertEquals(NoteVibration.DOUBLE, vibration)
        }
        with(NoteIntensity.BRUTAL.settings) {
            assertEquals(LabelBehavior.STICKY, behavior)
            assertEquals(Backdrop.FULL, backdrop)
            assertEquals(NoteSound.CHAOS, sound)
            assertTrue(loud)
            assertEquals(10, repeatSeconds)
        }
    }

    @Test fun anyTweakBecomesCustom() {
        val gentle = NoteIntensity.GENTLE.settings
        assertNull(NoteIntensity.detect(gentle.copy(snoozeMinutes = 60)))
        assertNull(NoteIntensity.detect(gentle.copy(sound = NoteSound.BEEP)))
        assertNull(NoteIntensity.detect(gentle.copy(vibration = NoteVibration.TAP)))
        assertNull(NoteIntensity.detect(gentle.copy(backdrop = Backdrop.HALF)))
        assertNull(NoteIntensity.detect(gentle.copy(behavior = LabelBehavior.CLOSABLE_FOREVER)))
        assertNull(NoteIntensity.detect(gentle.copy(loud = true)))
        assertNull(NoteIntensity.detect(NoteIntensity.FIRM.settings.copy(allowContinue = false)))
        assertNull(NoteIntensity.detect(NoteIntensity.BRUTAL.settings.copy(repeatSeconds = 30)))
    }

    @Test fun fieldsWithoutEffectAreIgnored() {
        // A sticky note never uses its period.
        assertEquals(NoteIntensity.BRUTAL, NoteIntensity.detect(NoteIntensity.BRUTAL.settings.copy(snoozeMinutes = 240)))
        val quiet = NoteIntensity.QUIET.settings
        // Continue only matters with a backdrop; loud and repeat only with alerts.
        assertEquals(NoteIntensity.QUIET, NoteIntensity.detect(quiet.copy(allowContinue = true)))
        assertEquals(NoteIntensity.QUIET, NoteIntensity.detect(quiet.copy(loud = true, repeatSeconds = 30)))
        // Loud only matters with sound or voice, not vibration alone.
        val buzzOnly = quiet.copy(vibration = NoteVibration.TAP)
        assertEquals(buzzOnly.normalized(), buzzOnly.copy(loud = true).normalized())
    }

    @Test fun applyingPresetSetsEveryField() {
        val custom = IntensitySettings(
            LabelBehavior.CLOSABLE_FOREVER, 240, Backdrop.HALF, true,
            NoteSound.HORN, true, NoteVibration.LONG, true, 60,
        )
        for (p in NoteIntensity.entries) assertEquals(p, NoteIntensity.detect(p.applyTo(custom)))
        assertEquals(NoteIntensity.GENTLE.settings, NoteIntensity.GENTLE.applyTo(custom))
    }

    @Test fun stickyPresetKeepsCurrentPeriod() {
        val current = NoteIntensity.GENTLE.settings.copy(snoozeMinutes = 90)
        val brutal = NoteIntensity.BRUTAL.applyTo(current)
        assertEquals(90, brutal.snoozeMinutes)
        assertEquals(LabelBehavior.STICKY, brutal.behavior)
    }

    @Test fun legacyDefaultLabelIsQuiet() {
        // Notes saved before presets used the entity defaults.
        assertEquals(NoteIntensity.QUIET, NoteIntensity.detect(IntensitySettings.of(LabelEntity("p", "hi"))))
    }

    @Test fun ofCopiesLabelFields() {
        val label = LabelEntity(
            packageName = "p", text = "hi", behavior = LabelBehavior.STICKY, snoozeMinutes = 10,
            backdrop = Backdrop.FULL, sound = NoteSound.CHAOS, voice = true, vibration = NoteVibration.LONG,
            repeatSeconds = 10, loud = true, allowContinue = true,
        )
        assertEquals(
            IntensitySettings(
                LabelBehavior.STICKY, 10, Backdrop.FULL, true,
                NoteSound.CHAOS, true, NoteVibration.LONG, true, 10,
            ),
            IntensitySettings.of(label),
        )
    }
}
