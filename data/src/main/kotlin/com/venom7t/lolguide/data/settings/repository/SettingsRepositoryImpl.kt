package com.venom7t.lolguide.data.settings.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.venom7t.lolguide.domain.settings.model.ThemeMode
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepositoryImpl constructor(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            // An unrecognised stored value (future rename) falls back to
            // following the system rather than crashing on launch.
            prefs[KEY_THEME_MODE]?.let { stored ->
                ThemeMode.entries.firstOrNull { it.name == stored }
            } ?: ThemeMode.SYSTEM
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    override fun observeApiKeyOverride(): Flow<String?> =
        dataStore.data.map { prefs -> prefs[KEY_API_KEY_OVERRIDE]?.takeIf { it.isNotBlank() } }

    override suspend fun setApiKeyOverride(key: String?) {
        dataStore.edit { prefs ->
            if (key.isNullOrBlank()) prefs.remove(KEY_API_KEY_OVERRIDE) else prefs[KEY_API_KEY_OVERRIDE] = key
        }
    }

    override fun observeHasCompletedFirstRun(): Flow<Boolean> =
        dataStore.data.map { prefs -> prefs[KEY_HAS_COMPLETED_FIRST_RUN] ?: false }

    override suspend fun markFirstRunCompleted() {
        dataStore.edit { it[KEY_HAS_COMPLETED_FIRST_RUN] = true }
    }

    private companion object {
        val KEY_THEME_MODE = stringPreferencesKey("settings_theme_mode")
        val KEY_API_KEY_OVERRIDE = stringPreferencesKey("settings_riot_api_key_override")
        val KEY_HAS_COMPLETED_FIRST_RUN = booleanPreferencesKey("settings_has_completed_first_run")
    }
}
