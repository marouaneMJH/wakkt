package com.wakkt.app.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "wakkt_settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext context: Context,
) : SettingsRepository {
    private val store = context.settingsDataStore

    override val labelsEnabled: Flow<Boolean> = store.data.map { it[LABELS_ENABLED] ?: true }

    override suspend fun setLabelsEnabled(enabled: Boolean) {
        store.edit { it[LABELS_ENABLED] = enabled }
    }

    override val onboardingDone: Flow<Boolean> = store.data.map { it[ONBOARDING_DONE] ?: false }

    override suspend fun setOnboardingDone(done: Boolean) {
        store.edit { it[ONBOARDING_DONE] = done }
    }

    private companion object {
        val LABELS_ENABLED = booleanPreferencesKey("labels_enabled")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    }
}
