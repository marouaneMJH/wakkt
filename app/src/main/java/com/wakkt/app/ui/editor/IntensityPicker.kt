package com.wakkt.app.ui.editor

import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.wakkt.app.R

/** The single main choice of the editor: how hard the note pushes. Null [selected] = Custom. */
@Composable
internal fun IntensityPicker(selected: NoteIntensity?, onSelect: (NoteIntensity) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionTitle(stringResource(R.string.editor_intensity), Modifier.weight(1f))
            if (selected == null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                ) {
                    Text(
                        stringResource(R.string.editor_intensity_custom),
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }
        }
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NoteIntensity.entries.chunked(2).forEach { pair ->
                Row(
                    Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { intensity ->
                        IntensityCard(
                            intensity = intensity,
                            selected = intensity == selected,
                            onClick = { onSelect(intensity) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
        if (selected == null) {
            Text(
                stringResource(R.string.editor_intensity_custom_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun IntensityCard(intensity: NoteIntensity, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    Surface(
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = content,
        border = BorderStroke(
            if (selected) 2.dp else 1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier
            .heightIn(min = 96.dp)
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    intensity.icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    stringResource(intensity.title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(
                stringResource(intensity.description),
                style = MaterialTheme.typography.bodySmall,
                color = if (selected) content else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@get:StringRes
internal val NoteIntensity.title: Int
    get() = when (this) {
        NoteIntensity.QUIET -> R.string.editor_intensity_quiet
        NoteIntensity.GENTLE -> R.string.editor_intensity_gentle
        NoteIntensity.FIRM -> R.string.editor_intensity_firm
        NoteIntensity.BRUTAL -> R.string.editor_intensity_brutal
    }

@get:StringRes
private val NoteIntensity.description: Int
    get() = when (this) {
        NoteIntensity.QUIET -> R.string.editor_intensity_quiet_desc
        NoteIntensity.GENTLE -> R.string.editor_intensity_gentle_desc
        NoteIntensity.FIRM -> R.string.editor_intensity_firm_desc
        NoteIntensity.BRUTAL -> R.string.editor_intensity_brutal_desc
    }

private val NoteIntensity.icon: ImageVector
    get() = when (this) {
        NoteIntensity.QUIET -> Icons.Rounded.NotificationsOff
        NoteIntensity.GENTLE -> Icons.Rounded.Notifications
        NoteIntensity.FIRM -> Icons.Rounded.NotificationsActive
        NoteIntensity.BRUTAL -> Icons.Rounded.Campaign
    }
