package com.wakkt.app.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labels")
data class LabelEntity(
    @PrimaryKey val packageName: String,
    val text: String,
    val behavior: LabelBehavior = LabelBehavior.CLOSABLE_PERIOD,
    val snoozeMinutes: Int = 60,
    val snoozedUntil: Long = 0L,
    val dismissedForever: Boolean = false,
    val enabled: Boolean = true,
    @ColumnInfo(defaultValue = "YELLOW") val color: NoteColor = NoteColor.YELLOW,
    @ColumnInfo(defaultValue = "CLASSIC") val style: NoteStyle = NoteStyle.CLASSIC,
    @ColumnInfo(defaultValue = "NONE") val backdrop: Backdrop = Backdrop.NONE,
    /** Absolute path of an image copied into app storage by [NoteImageStore], or null. */
    val imagePath: String? = null,
    // Attention: sounds and vibration (v3).
    @ColumnInfo(defaultValue = "NONE") val sound: NoteSound = NoteSound.NONE,
    /** Read the note text aloud with text-to-speech. */
    @ColumnInfo(defaultValue = "0") val voice: Boolean = false,
    @ColumnInfo(defaultValue = "NONE") val vibration: NoteVibration = NoteVibration.NONE,
    /** Repeat sound/voice/vibration every N seconds while the note is open; 0 = once. */
    @ColumnInfo(defaultValue = "0") val repeatSeconds: Int = 0,
    /** Play sound and voice at alarm volume, even when the phone is on silent (v4). */
    @ColumnInfo(defaultValue = "0") val loud: Boolean = false,
    // Legacy (v3): effects are now random per appearance (NoteEffects.random); columns kept.
    @ColumnInfo(defaultValue = "NONE") val motion: NoteMotion = NoteMotion.NONE,
    @ColumnInfo(defaultValue = "NONE") val pattern: NotePattern = NotePattern.NONE,
    @ColumnInfo(defaultValue = "0") val grain: Boolean = false,
    @ColumnInfo(defaultValue = "0") val glitch: Boolean = false,
    /** With a backdrop: show "Continue to app", which lifts the cover but keeps the note (v5). */
    @ColumnInfo(defaultValue = "0") val allowContinue: Boolean = false,
) {
    val hasAlerts: Boolean get() = sound != NoteSound.NONE || voice || vibration != NoteVibration.NONE

    companion object {
        /** Repeat choices offered in the editor (seconds). */
        val REPEAT_PRESETS = listOf(0, 10, 30, 60)
    }
}
