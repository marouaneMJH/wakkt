package com.wakkt.app.overlay

import androidx.compose.ui.graphics.Color
import com.wakkt.app.data.NoteColor

/** Paper and ink colors for a note, for the light or dark theme. */
data class NotePalette(val paper: Color, val ink: Color, val accent: Color)

fun NoteColor.palette(dark: Boolean): NotePalette = when (this) {
    NoteColor.YELLOW -> if (dark) NotePalette(Color(0xFF4A4120), Color(0xFFFFF1C2), Color(0xFFE8C547))
    else NotePalette(Color(0xFFFFF3B0), Color(0xFF3D3410), Color(0xFFE0B400))
    NoteColor.PINK -> if (dark) NotePalette(Color(0xFF4D2A35), Color(0xFFFFDDE6), Color(0xFFF08AA8))
    else NotePalette(Color(0xFFFFDDE6), Color(0xFF4A1C2A), Color(0xFFE5668C))
    NoteColor.MINT -> if (dark) NotePalette(Color(0xFF213F35), Color(0xFFD4F5E6), Color(0xFF6FD3A8))
    else NotePalette(Color(0xFFD4F5E6), Color(0xFF123A2B), Color(0xFF3DB585))
    NoteColor.SKY -> if (dark) NotePalette(Color(0xFF1F3550), Color(0xFFD8EAFF), Color(0xFF7DB4F0))
    else NotePalette(Color(0xFFD8EAFF), Color(0xFF14304F), Color(0xFF4A90E2))
    NoteColor.LAVENDER -> if (dark) NotePalette(Color(0xFF362B52), Color(0xFFE6DDFF), Color(0xFFB39BF0))
    else NotePalette(Color(0xFFE6DDFF), Color(0xFF2B1F4D), Color(0xFF8C6FE0))
    NoteColor.PEACH -> if (dark) NotePalette(Color(0xFF4D3324), Color(0xFFFFE2CF), Color(0xFFF2A577))
    else NotePalette(Color(0xFFFFE2CF), Color(0xFF4A2814), Color(0xFFE8834A))
}
