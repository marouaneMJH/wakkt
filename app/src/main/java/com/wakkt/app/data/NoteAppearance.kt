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
