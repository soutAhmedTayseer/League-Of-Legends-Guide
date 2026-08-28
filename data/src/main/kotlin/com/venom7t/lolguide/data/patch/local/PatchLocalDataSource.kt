package com.venom7t.lolguide.data.patch.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Persists the last successfully resolved patch version.
 *
 * DataStore rather than Room because this is a single scalar setting, and it
 * must be readable before the database is opened so the UI can paint cached
 * content without waiting on anything.
 */
@Singleton
class PatchLocalDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {

    suspend fun getPatch(): String? =
        dataStore.data.map { it[KEY_PATCH] }.first()

    suspend fun setPatch(version: String) {
        dataStore.edit { it[KEY_PATCH] = version }
    }

    private companion object {
        val KEY_PATCH = stringPreferencesKey("current_patch_version")
    }
}
