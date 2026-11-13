package com.wakkt.app.settings

import kotlinx.coroutines.flow.Flow

/** App-wide settings backed by DataStore. CONTRACT; implemented by the data agent. */
interface SettingsRepository {
    /** Global on/off switch for all labels. Default true. */
    val labelsEnabled: Flow<Boolean>
    suspend fun setLabelsEnabled(enabled: Boolean)

    /** Whether the user finished onboarding. Default false. */
    val onboardingDone: Flow<Boolean>
    suspend fun setOnboardingDone(done: Boolean)
}
