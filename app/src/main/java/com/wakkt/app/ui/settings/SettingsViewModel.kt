package com.wakkt.app.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.settings.SettingsRepository
import com.wakkt.app.ui.MonitorControl
import com.wakkt.app.ui.permissions.PermissionChecks
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val monitor: MonitorControl,
) : ViewModel() {
    val labelsEnabled: StateFlow<Boolean> =
        settings.labelsEnabled.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    val versionName: String = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    } catch (_: Exception) {
        ""
    }

    fun setLabelsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settings.setLabelsEnabled(enabled)
            if (enabled) {
                if (PermissionChecks.allGranted(context)) monitor.start()
            } else {
                monitor.stop()
            }
        }
    }
}
