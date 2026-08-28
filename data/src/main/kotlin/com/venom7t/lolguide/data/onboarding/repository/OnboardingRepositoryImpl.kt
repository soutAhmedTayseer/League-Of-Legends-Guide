package com.venom7t.lolguide.data.onboarding.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.venom7t.lolguide.domain.onboarding.model.OnboardingPreferences
import com.venom7t.lolguide.domain.onboarding.model.PrimaryRole
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.onboarding.repository.OnboardingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnboardingRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : OnboardingRepository {

    override fun observePreferences(): Flow<OnboardingPreferences> =
        dataStore.data.map { prefs ->
            OnboardingPreferences(
                // A stored value that no longer matches an enum constant
                // (e.g. after a future rename) falls back to "unset" rather
                // than crashing the app on every launch.
                region = prefs[KEY_REGION]?.let { stored ->
                    Region.entries.firstOrNull { it.name == stored }
                },
                primaryRole = prefs[KEY_ROLE]?.let { stored ->
                    PrimaryRole.entries.firstOrNull { it.name == stored }
                },
                hasCompletedOnboarding = prefs[KEY_COMPLETED] ?: false,
            )
        }

    override suspend fun setRegion(region: Region) {
        dataStore.edit { it[KEY_REGION] = region.name }
    }

    override suspend fun setPrimaryRole(role: PrimaryRole) {
        dataStore.edit { it[KEY_ROLE] = role.name }
    }

    override suspend fun markOnboardingComplete() {
        dataStore.edit { it[KEY_COMPLETED] = true }
    }

    private companion object {
        val KEY_REGION = stringPreferencesKey("onboarding_region")
        val KEY_ROLE = stringPreferencesKey("onboarding_primary_role")
        val KEY_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }
}
