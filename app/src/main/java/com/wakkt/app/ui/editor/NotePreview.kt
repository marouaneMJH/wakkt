package com.wakkt.app.ui.editor

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.wakkt.app.R
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.NoteEffects
import com.wakkt.app.overlay.ChaosCanvas
import com.wakkt.app.overlay.StickyNote
import com.wakkt.app.overlay.TvStaticLayer
import com.wakkt.app.overlay.WindowOverlayController

/** Preview header (Test sound, Shuffle), the live preview, and the random-visuals hint. */
@Composable
internal fun PreviewSection(state: EditorState, onTest: () -> Unit) {
    var seed by rememberSaveable { mutableLongStateOf(System.nanoTime()) }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(stringResource(R.string.editor_preview), Modifier.weight(1f))
            if (state.hasAlerts) {
                TextButton(onClick = onTest) {
                    Icon(Icons.Rounded.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.size(6.dp))
                    Text(stringResource(R.string.editor_test))
                }
            }
            TextButton(onClick = { seed = System.nanoTime() }) {
                Icon(Icons.Rounded.Shuffle, null, Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.editor_shuffle))
            }
        }
        NotePreview(state, seed)
        Text(
            stringResource(R.string.editor_chaos_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

/** The note over a mock app screen, showing how the backdrop will look. */
@Composable
private fun NotePreview(state: EditorState, seed: Long) {
    val effects = remember(seed) { NoteEffects.random(seed) }
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasBackdrop = state.backdrop != Backdrop.NONE
    val text = state.text.ifBlank { stringResource(R.string.editor_preview_placeholder) }
    val description = stringResource(R.string.editor_preview_cd, text)
    Box(
        Modifier
            .fillMaxWidth()
            .height(
                when {
                    state.imagePath != null -> 320.dp
                    hasBackdrop -> 260.dp
                    else -> 200.dp
                },
            )
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            // Decorative mock: read as one description, not as the fake app's parts.
            .clearAndSetSemantics { contentDescription = description },
    ) {
        // Mock app content.
        val blur = if (hasBackdrop && blurSupported && state.backdrop != Backdrop.FULL) (state.backdrop.strength * 16).dp else 0.dp
        Column(
            Modifier.fillMaxSize().blur(blur).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val bar = MaterialTheme.colorScheme.outlineVariant
            repeat(3) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(32.dp).clip(CircleShape).background(bar))
                    Spacer(Modifier.size(10.dp))
                    Box(Modifier.height(10.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(5.dp)).background(bar))
                }
                Box(Modifier.height(56.dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(bar.copy(alpha = 0.6f)))
            }
        }
        if (hasBackdrop) {
            val alpha = WindowOverlayController.scrimAlpha(state.backdrop, blurSupported)
            ChaosCanvas(seed + 1, Modifier.matchParentSize().graphicsLayer { this.alpha = alpha })
            if (effects.grain) TvStaticLayer(alpha = 0.22f, modifier = Modifier.matchParentSize())
        }
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (hasBackdrop) Arrangement.Center else Arrangement.Top,
        ) {
            StickyNote(
                text = text,
                closable = state.behavior != LabelBehavior.STICKY,
                onClose = {},
                style = state.style,
                imagePath = state.imagePath,
                maxWidth = 240.dp,
                effects = effects,
                chaosSeed = seed,
            )
            if (hasBackdrop && state.allowContinue) {
                FilledTonalButton(onClick = {}) { Text(stringResource(R.string.overlay_continue)) }
            }
        }
    }
}
