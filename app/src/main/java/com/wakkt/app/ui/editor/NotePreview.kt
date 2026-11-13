package com.wakkt.app.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// TODO(integration): replace with overlay.StickyNote
@Composable
internal fun NotePreview(text: String, closable: Boolean, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFFF1A8),
        shadowElevation = 6.dp,
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = if (closable) 8.dp else 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF3A3320),
                modifier = Modifier.weight(1f),
            )
            if (closable) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = null,
                    tint = Color(0xFF3A3320).copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = 8.dp).size(20.dp),
                )
            }
        }
    }
}
