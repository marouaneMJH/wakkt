package com.wakkt.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class LabelRulesTest {
    private fun label(
        behavior: LabelBehavior = LabelBehavior.CLOSABLE_PERIOD,
        text: String = "hi",
        enabled: Boolean = true,
        snoozedUntil: Long = 0L,
        dismissedForever: Boolean = false,
        snoozeMinutes: Int = 60,
    ) = LabelEntity("p", text, behavior, snoozeMinutes, snoozedUntil, dismissedForever, enabled)

    private fun millis(zone: ZoneId, y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, zone).toInstant().toEpochMilli()

    @Test fun disabledNeverShows() {
        for (b in LabelBehavior.values()) assertFalse(LabelRules.shouldShow(label(b, enabled = false), 1000))
    }

    @Test fun blankTextNeverShows() {
        for (b in LabelBehavior.values()) {
            assertFalse(LabelRules.shouldShow(label(b, text = ""), 1000))
            assertFalse(LabelRules.shouldShow(label(b, text = "   "), 1000))
        }
    }

    @Test fun stickyAlwaysShows() {
        val l = label(LabelBehavior.STICKY, dismissedForever = true, snoozedUntil = Long.MAX_VALUE)
        assertTrue(LabelRules.shouldShow(l, 1000))
    }

    @Test fun foreverHiddenOnlyWhenDismissed() {
        assertTrue(LabelRules.shouldShow(label(LabelBehavior.CLOSABLE_FOREVER), 1000))
        assertFalse(LabelRules.shouldShow(label(LabelBehavior.CLOSABLE_FOREVER, dismissedForever = true), 1000))
    }

    @Test fun periodHiddenUntilSnoozeEnds() {
        val l = label(snoozedUntil = 5000)
        assertFalse(LabelRules.shouldShow(l, 4999))
        assertTrue(LabelRules.shouldShow(l, 5000))
        assertTrue(LabelRules.shouldShow(l, 5001))
    }

    @Test fun dismissStickyUnchanged() {
        val l = label(LabelBehavior.STICKY)
        assertSame(l, LabelRules.afterDismiss(l, 1000))
    }

    @Test fun dismissForeverSetsFlag() {
        val r = LabelRules.afterDismiss(label(LabelBehavior.CLOSABLE_FOREVER), 1000)
        assertTrue(r.dismissedForever)
        assertFalse(LabelRules.shouldShow(r, 2000))
    }

    @Test fun dismissPeriodAddsMinutes() {
        val r = LabelRules.afterDismiss(label(snoozeMinutes = 15), 1_000)
        assertEquals(1_000 + 15 * 60_000L, r.snoozedUntil)
    }

    @Test fun nextMidnightUsesZone() {
        val zone = ZoneId.of("Europe/Paris")
        val now = millis(zone, 2026, 3, 10, 22, 30)
        assertEquals(millis(zone, 2026, 3, 11), LabelRules.nextMidnight(now, zone))
        // exactly midnight rolls to the following day
        assertEquals(millis(zone, 2026, 3, 12), LabelRules.nextMidnight(millis(zone, 2026, 3, 11), zone))
    }

    @Test fun nextMidnightAcrossDst() {
        val zone = ZoneId.of("Europe/Paris") // DST starts 2026-03-29
        val now = millis(zone, 2026, 3, 28, 12)
        assertEquals(millis(zone, 2026, 3, 29), LabelRules.nextMidnight(now, zone))
    }

    @Test fun untilTomorrowDismissUsesNextMidnight() {
        val now = 1_700_000_000_000L
        val r = LabelRules.afterDismiss(label(snoozeMinutes = LabelRules.UNTIL_TOMORROW), now)
        assertEquals(LabelRules.nextMidnight(now), r.snoozedUntil)
        assertTrue(r.snoozedUntil > now)
        assertTrue(r.snoozedUntil - now <= 25 * 3_600_000L)
        assertFalse(LabelRules.shouldShow(r, now))
        assertTrue(LabelRules.shouldShow(r, r.snoozedUntil))
    }

    @Test fun `custom period snoozes for exactly that long`() {
        val now = 1_000_000L
        val r = LabelRules.afterDismiss(label(snoozeMinutes = 45), now)
        org.junit.Assert.assertEquals(now + 45 * 60_000L, r.snoozedUntil)
        org.junit.Assert.assertFalse(LabelRules.shouldShow(r, now + 44 * 60_000L))
        org.junit.Assert.assertTrue(LabelRules.shouldShow(r, now + 45 * 60_000L))
    }

    @Test fun `period validation accepts presets, custom and tomorrow only`() {
        LabelRules.PERIOD_PRESETS.forEach { org.junit.Assert.assertTrue(LabelRules.isValidPeriod(it)) }
        org.junit.Assert.assertTrue(LabelRules.isValidPeriod(5))
        org.junit.Assert.assertTrue(LabelRules.isValidPeriod(10))
        org.junit.Assert.assertTrue(LabelRules.isValidPeriod(LabelRules.MAX_CUSTOM_MINUTES))
        org.junit.Assert.assertFalse(LabelRules.isValidPeriod(0))
        org.junit.Assert.assertFalse(LabelRules.isValidPeriod(-5))
        org.junit.Assert.assertFalse(LabelRules.isValidPeriod(LabelRules.MAX_CUSTOM_MINUTES + 1))
    }
}
