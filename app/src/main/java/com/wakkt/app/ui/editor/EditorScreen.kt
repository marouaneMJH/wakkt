package com.wakkt.app.ui.editor

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wakkt.app.R

/**
 * Note editor. The main path is: type the text, optionally pick an intensity, Save. A new note
 * starts on [NoteIntensity.DEFAULT], so text + Save is enough. Every detail lives in Customize.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
                title = { AppTitle(state) },
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
            // Stays above the keyboard, so Save is reachable right after typing.
            Surface(color = MaterialTheme.colorScheme.background) {
                Button(
                    onClick = viewModel::save,
                    enabled = state.canSave,
                    modifier = Modifier
                        .navigationBarsPadding()
                        .imePadding()
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp),
                ) { Text(stringResource(R.string.editor_save)) }
            }
        },
    ) { padding ->
        if (!state.loaded) return@Scaffold

        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            if (state.exists) HiddenBanner(state, viewModel::onShowAgain)
            NoteTextField(state, viewModel::onTextChange)
            PreviewSection(state, viewModel::testAlerts)
            IntensityPicker(state.intensity, viewModel::onIntensityChange)
            if (state.exists) {
                SwitchRow(
                    title = stringResource(R.string.editor_enabled),
                    description = stringResource(R.string.editor_enabled_desc),
                    checked = state.enabled,
                    onChange = viewModel::onEnabledChange,
                )
            }
            CustomizeSection(state, viewModel)
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
private fun AppTitle(state: EditorState) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val icon = state.icon
        if (icon != null) {
            Image(icon, null, Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(Modifier.size(12.dp))
        }
        Column {
            Text(state.appName, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                stringResource(if (state.exists) R.string.editor_title_edit else R.string.editor_title_new),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NoteTextField(state: EditorState, onChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    // New note: the text is the only required step, so start typing right away.
    if (!state.exists) LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    OutlinedTextField(
        value = state.text,
        onValueChange = onChange,
        label = { Text(stringResource(R.string.editor_text_label)) },
        placeholder = { Text(stringResource(R.string.editor_text_placeholder)) },
        supportingText = { Text("${state.text.length}/$MAX_LABEL_LENGTH") },
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        minLines = 2,
        maxLines = 4,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().focusRequester(focus),
    )
}

/** For a saved note that was closed: says why it isn't showing and offers to bring it back. */
@Composable
private fun HiddenBanner(state: EditorState, onShowAgain: () -> Unit) {
    val now = System.currentTimeMillis()
    val hidden = !state.resetDismissal && state.enabled && (state.dismissedForever || state.snoozedUntil > now)
    if (!hidden) return
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            Modifier.padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                if (state.dismissedForever) stringResource(R.string.editor_hidden_forever)
                else stringResource(
                    R.string.editor_hidden_until,
                    android.text.format.DateFormat.getTimeFormat(LocalContext.current).format(java.util.Date(state.snoozedUntil)),
                ),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onShowAgain) { Text(stringResource(R.string.editor_show_again)) }
        }
    }
}
