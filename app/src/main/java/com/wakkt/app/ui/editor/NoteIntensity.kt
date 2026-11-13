package com.wakkt.app.ui.editor

import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteVibration

/**
 * The fields an intensity preset controls. Text, style, image and the enabled flag are not part
 * of a preset: picking a preset never touches them.
 */
data class IntensitySettings(
    val behavior: LabelBehavior,
    val snoozeMinutes: Int,
    val backdrop: Backdrop,
    val allowContinue: Boolean,
    val sound: NoteSound,
    val voice: Boolean,
    val vibration: NoteVibration,
    val loud: Boolean,
    val repeatSeconds: Int,
) {
    val hasAlerts: Boolean get() = sound != NoteSound.NONE || voice || vibration != NoteVibration.NONE

    /**
     * Same settings with every field that has no effect reset to a neutral value, so two settings
     * that behave identically compare equal (e.g. the period of a sticky note is never used).
     */
    fun normalized(): IntensitySettings = copy(
        snoozeMinutes = if (behavior == LabelBehavior.CLOSABLE_PERIOD) snoozeMinutes else 0,
        allowContinue = backdrop != Backdrop.NONE && allowContinue,
        loud = (sound != NoteSound.NONE || voice) && loud,
        repeatSeconds = if (hasAlerts) repeatSeconds else 0,
    )

    companion object {
        fun of(label: LabelEntity) = IntensitySettings(
            behavior = label.behavior,
            snoozeMinutes = label.snoozeMinutes,
            backdrop = label.backdrop,
            allowContinue = label.allowContinue,
            sound = label.sound,
            voice = label.voice,
            vibration = label.vibration,
            loud = label.loud,
            repeatSeconds = label.repeatSeconds,
        )
    }
}

/**
 * One-tap bundles of behavior, cover and attention settings. Not stored: the current preset is
 * always derived from the note's fields with [detect], and anything else is "Custom".
 */
enum class NoteIntensity(val settings: IntensitySettings) {
    /** Silent, closable, back after an hour. Same as notes created before presets existed. */
    QUIET(
        IntensitySettings(
            behavior = LabelBehavior.CLOSABLE_PERIOD,
            snoozeMinutes = 60,
            backdrop = Backdrop.NONE,
            allowContinue = false,
            sound = NoteSound.NONE,
            voice = false,
            vibration = NoteVibration.NONE,
            loud = false,
            repeatSeconds = 0,
        ),
    ),

    /** Small closable note with a chime, back after 15 minutes. The default for new notes. */
    GENTLE(
        IntensitySettings(
            behavior = LabelBehavior.CLOSABLE_PERIOD,
            snoozeMinutes = 15,
            backdrop = Backdrop.NONE,
            allowContinue = false,
            sound = NoteSound.CHIME,
            voice = false,
            vibration = NoteVibration.NONE,
            loud = false,
            repeatSeconds = 0,
        ),
    ),

    /** Covers 80% of the app with a way through, beep and double buzz, back after 5 minutes. */
    FIRM(
        IntensitySettings(
            behavior = LabelBehavior.CLOSABLE_PERIOD,
            snoozeMinutes = 5,
            backdrop = Backdrop.STRONG,
            allowContinue = true,
            sound = NoteSound.BEEP,
            voice = false,
            vibration = NoteVibration.DOUBLE,
            loud = false,
            repeatSeconds = 0,
        ),
    ),

    /** Covers the whole app, can't be closed, loud chaos and long buzz every 10 seconds. */
    BRUTAL(
        IntensitySettings(
            behavior = LabelBehavior.STICKY,
            snoozeMinutes = 0,
            backdrop = Backdrop.FULL,
            allowContinue = false,
            sound = NoteSound.CHAOS,
            voice = false,
            vibration = NoteVibration.LONG,
            loud = true,
            repeatSeconds = 10,
        ),
    ),
    ;

    /**
     * [current] changed to this preset. A preset without a period keeps the current one, so
     * switching to "closable for a period" later in Customize restores what the user had.
     */
    fun applyTo(current: IntensitySettings): IntensitySettings =
        if (settings.behavior == LabelBehavior.CLOSABLE_PERIOD) settings
        else settings.copy(snoozeMinutes = current.snoozeMinutes)

    companion object {
        val DEFAULT = GENTLE

        /** The preset that behaves exactly like [settings], or null for "Custom". */
        fun detect(settings: IntensitySettings): NoteIntensity? {
            val n = settings.normalized()
            return entries.firstOrNull { it.settings.normalized() == n }
        }
    }
}
