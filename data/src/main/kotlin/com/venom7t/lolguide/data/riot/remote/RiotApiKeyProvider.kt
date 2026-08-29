package com.venom7t.lolguide.data.riot.remote

import com.venom7t.lolguide.data.common.di.ApplicationScope
import com.venom7t.lolguide.data.common.di.RiotApiKey
import com.venom7t.lolguide.domain.settings.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * The Riot API key every request actually uses: the user's own pasted key
 * (Account screen) if they have set one, otherwise the app's bundled key.
 *
 * Riot's development keys expire every 24 hours and the app cannot renew one
 * itself, so once the bundled key expires every keyed feature would stay
 * broken until the next app update -- letting the user paste their own key
 * is the only way to keep using those features between updates.
 *
 * [RiotAuthInterceptor] reads [currentKey] synchronously on every request, so
 * this keeps a plain, always-up-to-date field rather than re-querying
 * DataStore (which is asynchronous) per request.
 */
class RiotApiKeyProvider constructor(
    @RiotApiKey private val bundledKey: String,
    settingsRepository: SettingsRepository,
    @ApplicationScope scope: CoroutineScope,
) {
    @Volatile
    private var override: String? = null

    val currentKey: String
        get() = override?.takeIf { it.isNotBlank() } ?: bundledKey

    val hasOverride: Boolean
        get() = !override.isNullOrBlank()

    init {
        settingsRepository.observeApiKeyOverride()
            .onEach { override = it }
            .launchIn(scope)
    }
}
