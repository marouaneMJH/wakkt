package com.wakkt.app.overlay

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wakkt.app.ui.theme.WakktTheme

private val NoteShape = RoundedCornerShape(14.dp)

/** Small warm sticky note. Stateless; follows the system light/dark theme. */
@Composable
fun StickyNote(
    text: String,
    closable: Boolean,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val background = if (dark) Color(0xFF4A4120) else Color(0xFFFFF3B0)
    val content = if (dark) Color(0xFFFFF1C2) else Color(0xFF3D3410)
    // Outer padding leaves room for the shadow so it is not clipped by the window.
    Box(modifier = modifier.padding(8.dp)) {
        Surface(
            modifier = Modifier
                .widthIn(max = 264.dp)
                .shadow(6.dp, NoteShape),
            shape = NoteShape,
            color = background,
            contentColor = content,
        ) {
            Row(
                modifier = Modifier.padding(
                    start = 14.dp,
                    end = if (closable) 4.dp else 14.dp,
                    top = if (closable) 4.dp else 10.dp,
                    bottom = if (closable) 4.dp else 10.dp,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = text,
                    fontSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Medium,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 8,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (closable) {
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Note light", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun StickyNoteLightPreview() {
    WakktTheme(darkTheme = false) { StickyNote("Check your budget first", closable = true, onClose = {}) }
}

@Preview(name = "Note dark", showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun StickyNoteDarkPreview() {
    WakktTheme(darkTheme = true) { StickyNote("10 minutes max", closable = false, onClose = {}) }
}
