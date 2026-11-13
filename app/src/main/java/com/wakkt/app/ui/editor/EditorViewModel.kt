package com.wakkt.app.ui.editor

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRepository
import com.wakkt.app.data.LabelRules
import com.wakkt.app.ui.applist.InstalledAppsSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val MAX_LABEL_LENGTH = 120

data class EditorState(
    val loaded: Boolean = false,
    val appName: String = "",
    val icon: ImageBitmap? = null,
    val exists: Boolean = false,
    val text: String = "",
    val behavior: LabelBehavior = LabelBehavior.CLOSABLE_PERIOD,
    val snoozeMinutes: Int = 60,
    val enabled: Boolean = true,
    val finished: Boolean = false,
) {
    val canSave: Boolean get() = text.isNotBlank()
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LabelRepository,
    private val apps: InstalledAppsSource,
) : ViewModel() {
    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var original: LabelEntity? = null

    init {
        viewModelScope.launch {
            val app = apps.get(packageName)
            val label = repository.get(packageName)
            original = label
            _state.value = EditorState(
                loaded = true,
                appName = app?.name ?: packageName,
                icon = app?.icon,
                exists = label != null,
                text = label?.text.orEmpty(),
                behavior = label?.behavior ?: LabelBehavior.CLOSABLE_PERIOD,
                snoozeMinutes = label?.snoozeMinutes ?: 60,
                enabled = label?.enabled ?: true,
            )
        }
    }

    fun onTextChange(value: String) = _state.update { it.copy(text = value.take(MAX_LABEL_LENGTH)) }
    fun onBehaviorChange(value: LabelBehavior) = _state.update { it.copy(behavior = value) }
    fun onPeriodChange(minutes: Int) = _state.update { it.copy(snoozeMinutes = minutes) }
    fun onEnabledChange(value: Boolean) = _state.update { it.copy(enabled = value) }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            val old = original
            val minutes = if (s.snoozeMinutes in LabelRules.PERIOD_PRESETS) s.snoozeMinutes else 60
            val unchanged = old != null && old.behavior == s.behavior
            repository.save(
                LabelEntity(
                    packageName = packageName,
                    text = s.text.trim(),
                    behavior = s.behavior,
                    snoozeMinutes = minutes,
                    // Keep snooze/dismiss progress unless the behavior changed.
                    snoozedUntil = if (unchanged) old!!.snoozedUntil else 0L,
                    dismissedForever = if (unchanged) old!!.dismissedForever else false,
                    enabled = s.enabled,
                ),
            )
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.delete(packageName)
            _state.update { it.copy(finished = true) }
        }
    }
}
