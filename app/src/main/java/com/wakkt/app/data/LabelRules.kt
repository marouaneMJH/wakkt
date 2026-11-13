package com.wakkt.app.data

import java.time.Instant
import java.time.ZoneId

/** Pure visibility rules for a label. CONTRACT: shared by service and repository. */
object LabelRules {
    /** Whether [label] should be shown when its app is opened at [now] (epoch millis). */
    fun shouldShow(label: LabelEntity, now: Long): Boolean = when {
        !label.enabled || label.text.isBlank() -> false
        label.behavior == LabelBehavior.STICKY -> true
        label.behavior == LabelBehavior.CLOSABLE_FOREVER -> !label.dismissedForever
        else -> now >= label.snoozedUntil
    }

    /** The updated label after the user closes it at [now]. STICKY labels cannot be closed. */
    fun afterDismiss(label: LabelEntity, now: Long): LabelEntity = when (label.behavior) {
        LabelBehavior.STICKY -> label
        LabelBehavior.CLOSABLE_FOREVER -> label.copy(dismissedForever = true)
        LabelBehavior.CLOSABLE_PERIOD -> label.copy(
            snoozedUntil = if (label.snoozeMinutes == UNTIL_TOMORROW) nextMidnight(now)
            else now + label.snoozeMinutes * 60_000L,
        )
    }

    /** Start of the next local day after [now]. */
    fun nextMidnight(now: Long, zone: ZoneId = ZoneId.systemDefault()): Long =
        Instant.ofEpochMilli(now).atZone(zone).toLocalDate().plusDays(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()

    /** Preset periods offered in the editor (minutes). [UNTIL_TOMORROW] is resolved at dismiss time. */
    val PERIOD_PRESETS = listOf(5, 10, 15, 60, 240, UNTIL_TOMORROW)
    const val UNTIL_TOMORROW = -1

    /** Longest custom period the user can pick: 7 days. */
    const val MAX_CUSTOM_MINUTES = 7 * 24 * 60

    /** A preset, "until tomorrow", or a custom period of 1 minute to 7 days. */
    fun isValidPeriod(minutes: Int): Boolean = minutes == UNTIL_TOMORROW || minutes in 1..MAX_CUSTOM_MINUTES
}
