package com.wakkt.app.ui.editor

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.draw.blur
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.NoteEffects
import androidx.compose.material3.FilledTonalButton
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteVibration
import com.wakkt.app.overlay.TvStaticLayer
import androidx.compose.material.icons.rounded.PlayArrow
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.overlay.StickyNote
import com.wakkt.app.overlay.WindowOverlayController
import com.wakkt.app.overlay.ChaosCanvas
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakkt.app.R
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelRules

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }
    var customPeriodOpen by remember { mutableStateOf(false) }

    LaunchedEffect(state.finished) { if (state.finished) onBack() }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.editor_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.editor_back))
                    }
                },
                actions = {
                    if (state.exists) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Rounded.Delete, stringResource(R.string.editor_delete))
                        }
                    }
                },
            )
        },
        bottomBar = {
            Button(
                onClick = viewModel::save,
                enabled = state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .height(56.dp),
            ) { Text(stringResource(R.string.editor_save)) }
        },
    ) { padding ->
        if (!state.loaded) return@Scaffold
        val focus = remember { FocusRequester() }
        LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = state.icon
                if (icon != null) {
                    Image(icon, null, Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)))
                }
                Text(
                    state.appName,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = if (icon != null) 16.dp else 0.dp),
                )
            }

            OutlinedTextField(
                value = state.text,
                onValueChange = viewModel::onTextChange,
                label = { Text(stringResource(R.string.editor_text_label)) },
                supportingText = { Text("${state.text.length}/$MAX_LABEL_LENGTH") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().focusRequester(focus),
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.editor_behavior), style = MaterialTheme.typography.titleSmall)
                BehaviorOption(
                    selected = state.behavior == LabelBehavior.STICKY,
                    title = stringResource(R.string.editor_behavior_sticky),
                    description = stringResource(R.string.editor_behavior_sticky_desc),
                    onClick = { viewModel.onBehaviorChange(LabelBehavior.STICKY) },
                )
                BehaviorOption(
                    selected = state.behavior == LabelBehavior.CLOSABLE_PERIOD,
                    title = stringResource(R.string.editor_behavior_period),
                    description = stringResource(R.string.editor_behavior_period_desc),
                    onClick = { viewModel.onBehaviorChange(LabelBehavior.CLOSABLE_PERIOD) },
                )
                BehaviorOption(
                    selected = state.behavior == LabelBehavior.CLOSABLE_FOREVER,
                    title = stringResource(R.string.editor_behavior_forever),
                    description = stringResource(R.string.editor_behavior_forever_desc),
                    onClick = { viewModel.onBehaviorChange(LabelBehavior.CLOSABLE_FOREVER) },
                )
            }

            if (state.behavior == LabelBehavior.CLOSABLE_PERIOD) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.editor_period), style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
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

            if (state.exists) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.editor_enabled),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
                    Switch(checked = state.enabled, onCheckedChange = viewModel::onEnabledChange)
                }
                val now = System.currentTimeMillis()
                val hidden = !state.resetDismissal && state.enabled &&
                    (state.dismissedForever || state.snoozedUntil > now)
                if (hidden) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (state.dismissedForever) stringResource(R.string.editor_hidden_forever)
                                else stringResource(
                                    R.string.editor_hidden_until,
                                    android.text.format.DateFormat.getTimeFormat(LocalContext.current).format(java.util.Date(state.snoozedUntil)),
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = viewModel::onShowAgain) { Text(stringResource(R.string.editor_show_again)) }
                        }
                    }
                }
            }

            LookSection(state, viewModel)
            AttentionSection(state, viewModel)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                var seed by remember { mutableLongStateOf(System.nanoTime()) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(R.string.editor_preview),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.weight(1f),
                    )
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
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }

    if (customPeriodOpen) {
        CustomPeriodDialog(
            initialMinutes = state.snoozeMinutes.takeIf { it > 0 && it !in LabelRules.PERIOD_PRESETS } ?: 30,
            onDismiss = { customPeriodOpen = false },
            onConfirm = { customPeriodOpen = false; viewModel.onPeriodChange(it) },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.editor_delete_title)) },
            text = { Text(stringResource(R.string.editor_delete_message)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; viewModel.delete() }) {
                    Text(stringResource(R.string.editor_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.editor_cancel))
                }
            },
        )
    }
}

@Composable
private fun periodLabel(minutes: Int): String = when {
    minutes == LabelRules.UNTIL_TOMORROW -> stringResource(R.string.editor_period_tomorrow)
    minutes < 60 -> stringResource(R.string.editor_period_minutes, minutes)
    minutes % 60 == 0 -> stringResource(R.string.editor_period_hours, minutes / 60)
    else -> stringResource(R.string.editor_period_hours_minutes, minutes / 60, minutes % 60)
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

@Composable
private fun BehaviorOption(
    selected: Boolean,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainerLow,
        border = if (selected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.padding(end = 8.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun LookSection(state: EditorState, viewModel: EditorViewModel) {
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        viewModel.onImagePicked(it)
    }
    val launchPicker = {
        pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.editor_look), style = MaterialTheme.typography.titleMedium)

        // Style
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.editor_style), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NoteStyle.entries.forEach { st ->
                    FilterChip(
                        selected = state.style == st,
                        onClick = { viewModel.onStyleChange(st) },
                        label = { Text(styleName(st)) },
                    )
                }
            }
        }

        // Backdrop
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.editor_backdrop), style = MaterialTheme.typography.titleSmall)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Backdrop.entries.forEach { b ->
                    FilterChip(
                        selected = state.backdrop == b,
                        onClick = { viewModel.onBackdropChange(b) },
                        label = { Text(backdropName(b)) },
                    )
                }
            }
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

        // Image
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.editor_image), style = MaterialTheme.typography.titleSmall)
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

/** The note over a mock app screen, showing how the backdrop will look. */
@Composable
private fun NotePreview(state: EditorState, seed: Long) {
    val effects = remember(seed) { NoteEffects.random(seed) }
    val blurSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val hasBackdrop = state.backdrop != Backdrop.NONE
    val text = state.text.ifBlank { stringResource(R.string.editor_preview_placeholder) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(if (hasBackdrop || state.imagePath != null) 340.dp else 220.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
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

@Composable
private fun styleName(s: NoteStyle): String = stringResource(
    when (s) {
        NoteStyle.CLASSIC -> R.string.editor_style_classic
        NoteStyle.SOFT -> R.string.editor_style_soft
        NoteStyle.OUTLINE -> R.string.editor_style_outline
    },
)

@Composable
private fun backdropName(b: Backdrop): String = stringResource(
    when (b) {
        Backdrop.NONE -> R.string.editor_backdrop_none
        Backdrop.HALF -> R.string.editor_backdrop_half
        Backdrop.STRONG -> R.string.editor_backdrop_strong
        Backdrop.FULL -> R.string.editor_backdrop_full
    },
)

@Composable
private fun AttentionSection(state: EditorState, viewModel: EditorViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.editor_attention),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            OutlinedButton(onClick = viewModel::testAlerts, enabled = state.hasAlerts) {
                Icon(Icons.Rounded.PlayArrow, null, Modifier.size(18.dp))
                Spacer(Modifier.size(6.dp))
                Text(stringResource(R.string.editor_test))
            }
        }
        ChoiceRow(
            title = stringResource(R.string.editor_sound),
            options = NoteSound.entries,
            selected = state.sound,
            label = {
                stringResource(
                    when (it) {
                        NoteSound.NONE -> R.string.editor_sound_none
                        NoteSound.CHIME -> R.string.editor_sound_chime
                        NoteSound.BEEP -> R.string.editor_sound_beep
                        NoteSound.BUZZ -> R.string.editor_sound_buzz
                        NoteSound.ALARM -> R.string.editor_sound_alarm
                        NoteSound.HORN -> R.string.editor_sound_horn
                        NoteSound.CHAOS -> R.string.editor_sound_chaos
                    },
                )
            },
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
            label = {
                stringResource(
                    when (it) {
                        NoteVibration.NONE -> R.string.editor_vibration_none
                        NoteVibration.TAP -> R.string.editor_vibration_tap
                        NoteVibration.DOUBLE -> R.string.editor_vibration_double
                        NoteVibration.LONG -> R.string.editor_vibration_long
                    },
                )
            },
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
                label = {
                    when (it) {
                        0 -> stringResource(R.string.editor_repeat_once)
                        60 -> stringResource(R.string.editor_repeat_minute)
                        else -> stringResource(R.string.editor_repeat_seconds, it)
                    }
                },
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

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun <T> ChoiceRow(
    title: String,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

@Composable
private fun SwitchRow(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
