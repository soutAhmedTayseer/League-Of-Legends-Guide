package com.venom7t.lolguide.data.game.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.venom7t.lolguide.data.game.local.GameStatsDto
import com.venom7t.lolguide.data.game.local.RoundProgressDto
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.game.model.RoundProgress
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

/**
 * Round-in-progress and lifetime stats, one JSON blob per mode in the shared
 * preferences DataStore -- same store Phase 3's onboarding preferences use,
 * just its own keys. No new table: this is small, per-mode state, not
 * something that benefits from Room's query surface (Phase 6 plan §6.3).
 */
class GameProgressRepositoryImpl constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : GameProgressRepository {

    override fun observeRound(mode: GameMode): Flow<RoundProgress?> =
        dataStore.data.map { prefs ->
            prefs[roundKey(mode)]?.let { raw ->
                // A corrupt or outdated stored blob (e.g. a future field
                // rename) drops the saved round rather than crashing on
                // every launch -- the player just starts a fresh one.
                runCatching { json.decodeFromString(RoundProgressDto.serializer(), raw).toDomain() }
                    .getOrNull()
            }
        }

    override suspend fun saveRound(round: RoundProgress) {
        val raw = json.encodeToString(RoundProgressDto.serializer(), RoundProgressDto.fromDomain(round))
        dataStore.edit { it[roundKey(round.mode)] = raw }
    }

    override fun observeStats(mode: GameMode): Flow<GameStats> =
        dataStore.data.map { prefs ->
            prefs[statsKey(mode)]?.let { raw ->
                runCatching { json.decodeFromString(GameStatsDto.serializer(), raw).toDomain() }
                    .getOrNull()
            } ?: GameStats(mode = mode)
        }

    override suspend fun saveStats(stats: GameStats) {
        val raw = json.encodeToString(GameStatsDto.serializer(), GameStatsDto.fromDomain(stats))
        dataStore.edit { it[statsKey(stats.mode)] = raw }
    }

    private fun roundKey(mode: GameMode) = stringPreferencesKey("game_round_${mode.name}")
    private fun statsKey(mode: GameMode) = stringPreferencesKey("game_stats_${mode.name}")
}
