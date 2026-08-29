package com.venom7t.lolguide.domain.settings.repository

import com.venom7t.lolguide.domain.settings.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** The app's own preferences -- theme only for now; language is an OS-level setting (AppCompatDelegate). */
interface SettingsRepository {
    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    /**
     * A user-pasted Riot API key that overrides the app's own, for when the
     * bundled key expires -- Riot's development keys are short-lived and the
     * app cannot ship a renewal itself. Null (or blank) means "use the
     * app's own key".
     */
    fun observeApiKeyOverride(): Flow<String?>
    suspend fun setApiKeyOverride(key: String?)

    /**
     * Whether the app has completed a full first run before -- gates the
     * loading skeletons' minimum-visible-duration polish (see
     * `rememberMinimumVisibleLoading`) to the very first time the app is
     * ever opened. After that, cached data usually loads fast enough that
     * forcing the same multi-second reveal on every visit would just be
     * friction, not polish.
     */
    fun observeHasCompletedFirstRun(): Flow<Boolean>
    suspend fun markFirstRunCompleted()
}
