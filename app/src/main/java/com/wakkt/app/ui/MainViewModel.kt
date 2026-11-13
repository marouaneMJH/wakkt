package com.wakkt.app.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.settings.SettingsRepository
import com.wakkt.app.ui.navigation.Routes
import com.wakkt.app.ui.permissions.PermissionChecks
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settings: SettingsRepository,
    private val monitor: MonitorControl,
) : ViewModel() {
    private val _startDestination = MutableStateFlow<String?>(null)

    /** Null until resolved. */
    val startDestination: StateFlow<String?> = _startDestination

    init {
        viewModelScope.launch {
            val ready = settings.onboardingDone.first() && PermissionChecks.allGranted(context)
            if (ready && settings.labelsEnabled.first()) monitor.start()
            _startDestination.value = if (ready) Routes.APP_LIST else Routes.ONBOARDING
        }
    }
}
