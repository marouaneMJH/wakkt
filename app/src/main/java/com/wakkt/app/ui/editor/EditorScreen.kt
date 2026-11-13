package com.wakkt.app.ui.editor

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
import com.wakkt.app.overlay.StickyNote
import com.wakkt.app.data.LabelRules

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

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
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.editor_preview), style = MaterialTheme.typography.titleSmall)
                StickyNote(
                    text = state.text.ifBlank { stringResource(R.string.editor_preview_placeholder) },
                    closable = state.behavior != LabelBehavior.STICKY,
                    onClose = {},
                )
            }
            Spacer(Modifier.height(8.dp))
        }
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
private fun periodLabel(minutes: Int): String = when (minutes) {
    15 -> stringResource(R.string.editor_period_15m)
    60 -> stringResource(R.string.editor_period_1h)
    240 -> stringResource(R.string.editor_period_4h)
    LabelRules.UNTIL_TOMORROW -> stringResource(R.string.editor_period_tomorrow)
    else -> stringResource(R.string.editor_period_minutes, minutes)
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
