package com.wakkt.app.ui.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.wakkt.app.R
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelRules
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.data.NoteVibration

/** A small section title, exposed to screen readers as a heading. */
@Composable
internal fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        modifier = modifier.semantics { heading() },
    )
}

/** A titled row of single-choice chips. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        SectionTitle(title)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.selectableGroup(),
        ) {
            options.forEach { option ->
                FilterChip(
                    selected = option == selected,
                    onClick = { onSelect(option) },
                    label = { Text(label(option)) },
                )
            }
        }
    }
}

/** A title + description with a switch; the whole row toggles. */
@Composable
internal fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** A large radio option with a title and a one-line description. */
@Composable
internal fun RadioCard(selected: Boolean, title: String, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        shape = shape,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(12.dp))
            Column(Modifier.padding(end = 8.dp)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun periodLabel(minutes: Int): String = when {
    minutes == LabelRules.UNTIL_TOMORROW -> stringResource(R.string.editor_period_tomorrow)
    minutes < 60 -> stringResource(R.string.editor_period_minutes, minutes)
    minutes % 60 == 0 -> stringResource(R.string.editor_period_hours, minutes / 60)
    else -> stringResource(R.string.editor_period_hours_minutes, minutes / 60, minutes % 60)
}

@Composable
internal fun styleName(s: NoteStyle): String = stringResource(
    when (s) {
        NoteStyle.CLASSIC -> R.string.editor_style_classic
        NoteStyle.SOFT -> R.string.editor_style_soft
        NoteStyle.OUTLINE -> R.string.editor_style_outline
    },
)

@Composable
internal fun backdropName(b: Backdrop): String = stringResource(
    when (b) {
        Backdrop.NONE -> R.string.editor_backdrop_none
        Backdrop.HALF -> R.string.editor_backdrop_half
        Backdrop.STRONG -> R.string.editor_backdrop_strong
        Backdrop.FULL -> R.string.editor_backdrop_full
    },
)

@Composable
internal fun soundName(s: NoteSound): String = stringResource(
    when (s) {
        NoteSound.NONE -> R.string.editor_sound_none
        NoteSound.CHIME -> R.string.editor_sound_chime
        NoteSound.BEEP -> R.string.editor_sound_beep
        NoteSound.BUZZ -> R.string.editor_sound_buzz
        NoteSound.ALARM -> R.string.editor_sound_alarm
        NoteSound.HORN -> R.string.editor_sound_horn
        NoteSound.CHAOS -> R.string.editor_sound_chaos
    },
)

@Composable
internal fun vibrationName(v: NoteVibration): String = stringResource(
    when (v) {
        NoteVibration.NONE -> R.string.editor_vibration_none
        NoteVibration.TAP -> R.string.editor_vibration_tap
        NoteVibration.DOUBLE -> R.string.editor_vibration_double
        NoteVibration.LONG -> R.string.editor_vibration_long
    },
)

@Composable
internal fun repeatName(seconds: Int): String = when (seconds) {
    0 -> stringResource(R.string.editor_repeat_once)
    60 -> stringResource(R.string.editor_repeat_minute)
    else -> stringResource(R.string.editor_repeat_seconds, seconds)
}
