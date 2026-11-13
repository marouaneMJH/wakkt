package com.wakkt.app.ui.editor

import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.data.Backdrop
import com.wakkt.app.data.LabelBehavior
import com.wakkt.app.data.LabelEntity
import com.wakkt.app.data.LabelRepository
import com.wakkt.app.data.LabelRules
import com.wakkt.app.data.NoteImageStore
import com.wakkt.app.data.NoteStyle
import com.wakkt.app.data.NoteEffects
import com.wakkt.app.data.NoteMotion
import com.wakkt.app.data.NoteSound
import com.wakkt.app.data.NoteVibration
import com.wakkt.app.alerts.NoteAlerter
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
    /** Dismissal state of the saved label, shown so the user can bring the note back. */
    val dismissedForever: Boolean = false,
    val snoozedUntil: Long = 0L,
    val resetDismissal: Boolean = false,
    val style: NoteStyle = NoteStyle.CLASSIC,
    val backdrop: Backdrop = Backdrop.NONE,
    val imagePath: String? = null,
    val sound: NoteSound = NoteSound.NONE,
    val voice: Boolean = false,
    val vibration: NoteVibration = NoteVibration.NONE,
    val repeatSeconds: Int = 0,
    val loud: Boolean = false,
    val motion: NoteMotion = NoteMotion.NONE,
    val grain: Boolean = false,
    val glitch: Boolean = false,
    val importingImage: Boolean = false,
    val imageError: Boolean = false,
    val finished: Boolean = false,
) {
    val canSave: Boolean get() = text.isNotBlank()
    val hasAlerts: Boolean get() = sound != NoteSound.NONE || voice || vibration != NoteVibration.NONE
    val effects: NoteEffects get() = NoteEffects(motion, grain, glitch)
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: LabelRepository,
    private val apps: InstalledAppsSource,
    private val images: NoteImageStore,
    private val alerter: NoteAlerter,
) : ViewModel() {
    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _state = MutableStateFlow(EditorState())
    val state: StateFlow<EditorState> = _state.asStateFlow()

    private var original: LabelEntity? = null
    /** Images imported in this session that may need deleting if not kept. */
    private val importedImages = mutableSetOf<String>()
    private var saved = false

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
                dismissedForever = label?.dismissedForever ?: false,
                snoozedUntil = label?.snoozedUntil ?: 0L,
                style = label?.style ?: NoteStyle.CLASSIC,
                backdrop = label?.backdrop ?: Backdrop.NONE,
                imagePath = label?.imagePath,
                sound = label?.sound ?: NoteSound.NONE,
                voice = label?.voice ?: false,
                vibration = label?.vibration ?: NoteVibration.NONE,
                repeatSeconds = label?.repeatSeconds ?: 0,
                loud = label?.loud ?: false,
                motion = label?.motion ?: NoteMotion.NONE,
                grain = label?.grain ?: false,
                glitch = label?.glitch ?: false,
            )
        }
    }

    fun onTextChange(value: String) = _state.update { it.copy(text = value.take(MAX_LABEL_LENGTH)) }
    fun onBehaviorChange(value: LabelBehavior) = _state.update { it.copy(behavior = value) }
    fun onPeriodChange(minutes: Int) = _state.update { it.copy(snoozeMinutes = minutes) }
    fun onEnabledChange(value: Boolean) = _state.update { it.copy(enabled = value) }
    fun onShowAgain() = _state.update { it.copy(resetDismissal = true) }
    fun onStyleChange(value: NoteStyle) = _state.update { it.copy(style = value) }
    fun onBackdropChange(value: Backdrop) = _state.update { it.copy(backdrop = value) }
    fun onRemoveImage() = _state.update { it.copy(imagePath = null) }
    fun onSoundChange(value: NoteSound) = _state.update { it.copy(sound = value) }
    fun onVoiceChange(value: Boolean) = _state.update { it.copy(voice = value) }
    fun onVibrationChange(value: NoteVibration) = _state.update { it.copy(vibration = value) }
    fun onRepeatChange(seconds: Int) = _state.update { it.copy(repeatSeconds = seconds) }
    fun onLoudChange(value: Boolean) = _state.update { it.copy(loud = value) }
    fun onMotionChange(value: NoteMotion) = _state.update { it.copy(motion = value) }
    fun onGrainChange(value: Boolean) = _state.update { it.copy(grain = value) }
    fun onGlitchChange(value: Boolean) = _state.update { it.copy(glitch = value) }

    /** Plays the current sound, voice and vibration once. */
    fun testAlerts() {
        val s = _state.value
        alerter.test(draft(s).copy(text = s.text.trim()))
    }

    private fun draft(s: EditorState) = LabelEntity(
        packageName = packageName,
        text = s.text.trim(),
        behavior = s.behavior,
        snoozeMinutes = if (LabelRules.isValidPeriod(s.snoozeMinutes)) s.snoozeMinutes else 60,
        enabled = s.enabled,
        style = s.style,
        backdrop = s.backdrop,
        imagePath = s.imagePath,
        sound = s.sound,
        voice = s.voice,
        vibration = s.vibration,
        repeatSeconds = if (s.repeatSeconds in LabelEntity.REPEAT_PRESETS) s.repeatSeconds else 0,
        loud = s.loud,
        motion = s.motion,
        grain = s.grain,
        glitch = s.glitch,
    )

    fun onImagePicked(uri: Uri?) {
        if (uri == null) return
        _state.update { it.copy(importingImage = true, imageError = false) }
        viewModelScope.launch {
            val path = images.import(uri, packageName)
            if (path != null) importedImages += path
            _state.update {
                if (path != null) it.copy(imagePath = path, importingImage = false)
                else it.copy(importingImage = false, imageError = true)
            }
        }
    }

    fun save() {
        val s = _state.value
        if (!s.canSave) return
        viewModelScope.launch {
            val old = original
            // Keep snooze/dismiss progress unless the behavior changed, the user asked to show it
            // again, or re-enabled the label (PRD: "unless the user turns it back on inside wakkt").
            val keep = old != null && old.behavior == s.behavior && !s.resetDismissal && !(!old.enabled && s.enabled)
            repository.save(
                draft(s).copy(
                    snoozedUntil = if (keep) old!!.snoozedUntil else 0L,
                    dismissedForever = if (keep) old!!.dismissedForever else false,
                ),
            )
            saved = true
            // Drop files that are no longer used: the replaced original and unused imports.
            if (old?.imagePath != s.imagePath) images.delete(old?.imagePath)
            (importedImages - setOfNotNull(s.imagePath)).forEach { images.delete(it) }
            importedImages.clear()
            _state.update { it.copy(finished = true) }
        }
    }

    fun delete() {
        viewModelScope.launch {
            repository.delete(packageName)
            saved = true
            images.delete(original?.imagePath)
            importedImages.forEach { images.delete(it) }
            importedImages.clear()
            _state.update { it.copy(finished = true) }
        }
    }

    override fun onCleared() {
        alerter.stop()
        // Leaving without saving: discard images imported in this session.
        if (!saved && importedImages.isNotEmpty()) {
            importedImages.forEach(images::deleteNow)
            importedImages.clear()
        }
    }
}
