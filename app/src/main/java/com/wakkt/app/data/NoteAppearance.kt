package com.wakkt.app.data

/** Paper color of the note. Each has a light and a dark variant (see overlay/NoteColors.kt). */
enum class NoteColor { YELLOW, PINK, MINT, SKY, LAVENDER, PEACH }

/** Visual style of the note. */
enum class NoteStyle {
    CLASSIC,  // paper sticky note: square corners, folded corner, slight tilt
    SOFT,     // rounded card with a soft shadow
    OUTLINE,  // light surface with a colored edge
}

/** How much of the app behind the note is hidden. */
enum class Backdrop(val strength: Float) {
    NONE(0f),
    HALF(0.5f),
    STRONG(0.8f),
    FULL(1f),
}

/** Tone played when the note appears. Synthesized in-app (see alerts/ToneSynth.kt). */
enum class NoteSound { NONE, CHIME, BEEP, BUZZ, ALARM, HORN, CHAOS }

/** Vibration played when the note appears. */
enum class NoteVibration { NONE, TAP, DOUBLE, LONG }

/** Animation of the note itself. */
enum class NoteMotion { NONE, PULSE, SHAKE, BOUNCE, FLASH }

/** Pattern printed on the note paper. */
enum class NotePattern { NONE, STRIPES, DOTS, GRID, ZIGZAG }

/** Visual effects drawn by StickyNote. */
data class NoteEffects(
    val motion: NoteMotion = NoteMotion.NONE,
    val pattern: NotePattern = NotePattern.NONE,
    val grain: Boolean = false,
    val glitch: Boolean = false,
)
