package com.wakkt.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wakkt.app.settings.SettingsRepository
import com.wakkt.app.ui.MonitorControl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settings: SettingsRepository,
    private val monitor: MonitorControl,
) : ViewModel() {
    fun complete(onDone: () -> Unit) {
        viewModelScope.launch {
            settings.setOnboardingDone(true)
            if (settings.labelsEnabled.first()) monitor.start()
            onDone()
        }
    }
}
