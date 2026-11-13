package com.wakkt.app.data

/** Legacy: no longer shown; notes use random chaos visuals. Kept for the database column. */
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

/** Legacy: no longer shown; notes use random chaos visuals. Kept for the database column. */
enum class NotePattern { NONE, STRIPES, DOTS, GRID, ZIGZAG }

/** Visual effects drawn by StickyNote. */
data class NoteEffects(
    val motion: NoteMotion = NoteMotion.NONE,
    val grain: Boolean = false,
    val glitch: Boolean = false,
) {
    companion object {
        /**
         * Effects are not chosen by the user: every appearance rolls its own. There is always some
         * motion; TV static and glitch each show up about half the time.
         */
        fun random(seed: Long): NoteEffects {
            val r = kotlin.random.Random(seed xor 0x2545F4914F6CDD1DL)
            val motions = NoteMotion.entries.filter { it != NoteMotion.NONE }
            return NoteEffects(motion = motions[r.nextInt(motions.size)], grain = r.nextBoolean(), glitch = r.nextBoolean())
        }
    }
}
