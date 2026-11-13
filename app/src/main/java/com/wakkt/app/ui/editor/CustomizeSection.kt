package com.wakkt.app.ui.editor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wakkt.app.R
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRules
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.data.NoteVibration

/**
 * Every detail behind one collapsed row. The header shows a one-line summary so the current
 * setup is readable without expanding. Changing anything here makes the preset "Custom".
 */
@Composable
internal fun CustomizeSection(state: EditorState, viewModel: EditorViewModel) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column {
            val stateText = stringResource(if (expanded) R.string.editor_expanded else R.string.editor_collapsed)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 64.dp)
                    .clip(shape)
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .semantics { stateDescription = stateText }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Rounded.Tune, null, tint = MaterialTheme.colorScheme.primary)
                Column(Modifier.weight(1f).padding(horizontal = 14.dp)) {
                    Text(stringResource(R.string.editor_customize), style = MaterialTheme.typography.titleMedium)
                    Text(
                        summary(state),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, null)
            }
            AnimatedVisibility(expanded) {
                Column(
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    BehaviorGroup(state, viewModel)
                    HorizontalDivider()
                    CoverGroup(state, viewModel)
                    HorizontalDivider()
                    AttentionGroup(state, viewModel)
                    HorizontalDivider()
                    LookGroup(state, viewModel)
                }
            }
        }
    }
}

/** E.g. "Back in 15 min · Chime" or "Can't be closed · Hides the app · Chaos · Loud". */
@Composable
private fun summary(state: EditorState): String {
    val parts = mutableListOf<String>()
    parts += when (state.behavior) {
        LabelBehavior.STICKY -> stringResource(R.string.editor_summary_sticky)
        LabelBehavior.CLOSABLE_PERIOD -> stringResource(R.string.editor_summary_period, periodLabel(state.snoozeMinutes))
        LabelBehavior.CLOSABLE_FOREVER -> stringResource(R.string.editor_summary_forever)
    }
    when (state.backdrop) {
        Backdrop.NONE -> Unit
        Backdrop.HALF -> parts += stringResource(R.string.editor_summary_cover_half)
        Backdrop.STRONG -> parts += stringResource(R.string.editor_summary_cover_strong)
        Backdrop.FULL -> parts += stringResource(R.string.editor_summary_cover_full)
    }
    if (state.sound != NoteSound.NONE) parts += soundName(state.sound)
    if (state.voice) parts += stringResource(R.string.editor_summary_voice)
    if (state.vibration != NoteVibration.NONE) parts += stringResource(R.string.editor_summary_vibration)
    if (!state.hasAlerts) parts += stringResource(R.string.editor_summary_silent)
    if (state.loud && (state.sound != NoteSound.NONE || state.voice)) parts += stringResource(R.string.editor_loud)
    if (state.hasAlerts && state.repeatSeconds > 0) parts += repeatName(state.repeatSeconds)
    return parts.joinToString(" · ")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BehaviorGroup(state: EditorState, viewModel: EditorViewModel) {
    var customPeriodOpen by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(stringResource(R.string.editor_behavior))
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RadioCard(
                selected = state.behavior == LabelBehavior.CLOSABLE_PERIOD,
                title = stringResource(R.string.editor_behavior_period),
                description = stringResource(R.string.editor_behavior_period_desc),
                onClick = { viewModel.onBehaviorChange(LabelBehavior.CLOSABLE_PERIOD) },
            )
            RadioCard(
                selected = state.behavior == LabelBehavior.CLOSABLE_FOREVER,
                title = stringResource(R.string.editor_behavior_forever),
                description = stringResource(R.string.editor_behavior_forever_desc),
                onClick = { viewModel.onBehaviorChange(LabelBehavior.CLOSABLE_FOREVER) },
            )
            RadioCard(
                selected = state.behavior == LabelBehavior.STICKY,
                title = stringResource(R.string.editor_behavior_sticky),
                description = stringResource(R.string.editor_behavior_sticky_desc),
                onClick = { viewModel.onBehaviorChange(LabelBehavior.STICKY) },
            )
        }
        if (state.behavior == LabelBehavior.CLOSABLE_PERIOD) {
            SectionTitle(stringResource(R.string.editor_period), Modifier.padding(top = 8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
                LabelRules.PERIOD_PRESETS.forEach { minutes ->
                    FilterChip(
                        selected = state.snoozeMinutes == minutes,
                        onClick = { viewModel.onPeriodChange(minutes) },
                        label = { Text(periodLabel(minutes)) },
                    )
                }
                val custom = state.snoozeMinutes !in LabelRules.PERIOD_PRESETS
                FilterChip(
                    selected = custom,
                    onClick = { customPeriodOpen = true },
                    leadingIcon = { Icon(Icons.Rounded.Edit, null, Modifier.size(16.dp)) },
                    label = {
                        Text(
                            if (custom) stringResource(R.string.editor_period_custom_value, periodLabel(state.snoozeMinutes))
                            else stringResource(R.string.editor_period_custom),
                        )
                    },
                )
            }
        }
    }
    if (customPeriodOpen) {
        CustomPeriodDialog(
            initialMinutes = state.snoozeMinutes.takeIf { it > 0 && it !in LabelRules.PERIOD_PRESETS } ?: 30,
            onDismiss = { customPeriodOpen = false },
            onConfirm = { customPeriodOpen = false; viewModel.onPeriodChange(it) },
        )
    }
}

@Composable
private fun CoverGroup(state: EditorState, viewModel: EditorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        ChoiceRow(
            title = stringResource(R.string.editor_backdrop),
            options = Backdrop.entries,
            selected = state.backdrop,
            label = { backdropName(it) },
            onSelect = viewModel::onBackdropChange,
        )
        if (state.backdrop != Backdrop.NONE) {
            Text(
                stringResource(
                    when {
                        state.allowContinue -> R.string.editor_backdrop_hint_continue
                        state.behavior == LabelBehavior.STICKY -> R.string.editor_backdrop_hint_sticky
                        else -> R.string.editor_backdrop_hint
                    },
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SwitchRow(
                title = stringResource(R.string.editor_allow_continue),
                description = stringResource(R.string.editor_allow_continue_desc),
                checked = state.allowContinue,
                onChange = viewModel::onAllowContinueChange,
            )
        }
    }
}

@Composable
private fun AttentionGroup(state: EditorState, viewModel: EditorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceRow(
            title = stringResource(R.string.editor_sound),
            options = NoteSound.entries,
            selected = state.sound,
            label = { soundName(it) },
            onSelect = viewModel::onSoundChange,
        )
        SwitchRow(
            title = stringResource(R.string.editor_voice),
            description = stringResource(R.string.editor_voice_desc),
            checked = state.voice,
            onChange = viewModel::onVoiceChange,
        )
        ChoiceRow(
            title = stringResource(R.string.editor_vibration),
            options = NoteVibration.entries,
            selected = state.vibration,
            label = { vibrationName(it) },
            onSelect = viewModel::onVibrationChange,
        )
        if (state.sound != NoteSound.NONE || state.voice) {
            SwitchRow(
                title = stringResource(R.string.editor_loud),
                description = stringResource(R.string.editor_loud_desc),
                checked = state.loud,
                onChange = viewModel::onLoudChange,
            )
        }
        if (state.hasAlerts) {
            ChoiceRow(
                title = stringResource(R.string.editor_repeat),
                options = LabelEntity.REPEAT_PRESETS,
                selected = state.repeatSeconds,
                label = { repeatName(it) },
                onSelect = viewModel::onRepeatChange,
            )
            Text(
                stringResource(if (state.loud) R.string.editor_attention_hint_loud else R.string.editor_attention_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LookGroup(state: EditorState, viewModel: EditorViewModel) {
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        viewModel.onImagePicked(it)
    }
    val launchPicker = {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ChoiceRow(
            title = stringResource(R.string.editor_style),
            options = NoteStyle.entries,
            selected = state.style,
            label = { styleName(it) },
            onSelect = viewModel::onStyleChange,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle(stringResource(R.string.editor_image))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (state.imagePath == null) {
                    OutlinedButton(onClick = launchPicker, enabled = !state.importingImage) {
                        Icon(Icons.Rounded.AddPhotoAlternate, null, Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(stringResource(R.string.editor_image_add))
                    }
                } else {
                    OutlinedButton(onClick = launchPicker, enabled = !state.importingImage) {
                        Text(stringResource(R.string.editor_image_change))
                    }
                    TextButton(onClick = viewModel::onRemoveImage, enabled = !state.importingImage) {
                        Text(stringResource(R.string.editor_image_remove))
                    }
                }
                if (state.importingImage) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            }
            if (state.imageError) {
                Text(
                    stringResource(R.string.editor_image_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

/** Lets the user pick any period from 1 minute to 7 days as hours + minutes. */
@Composable
private fun CustomPeriodDialog(initialMinutes: Int, onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var hours by remember { mutableStateOf((initialMinutes / 60).toString()) }
    var minutes by remember { mutableStateOf((initialMinutes % 60).toString()) }
    val total = (hours.toIntOrNull() ?: 0) * 60 + (minutes.toIntOrNull() ?: 0)
    val valid = total in 1..LabelRules.MAX_CUSTOM_MINUTES
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.editor_period_custom_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    NumberField(hours, { hours = it }, stringResource(R.string.editor_period_hours_label), Modifier.weight(1f))
                    NumberField(minutes, { minutes = it }, stringResource(R.string.editor_period_minutes_label), Modifier.weight(1f))
                }
                Text(
                    if (valid) stringResource(R.string.editor_period_custom_preview, periodLabel(total))
                    else stringResource(R.string.editor_period_custom_error),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (valid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(total) }, enabled = valid) { Text(stringResource(R.string.editor_period_custom_set)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.editor_cancel)) } },
    )
}

@Composable
private fun NumberField(value: String, onChange: (String) -> Unit, label: String, modifier: Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = { v -> onChange(v.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier,
    )
}
