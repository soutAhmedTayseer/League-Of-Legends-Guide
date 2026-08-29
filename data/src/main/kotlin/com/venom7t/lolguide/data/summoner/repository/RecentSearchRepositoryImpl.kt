package com.venom7t.lolguide.data.summoner.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.RecentSummonerSearch
import com.venom7t.lolguide.domain.summoner.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Encoded as a single delimited string rather than pulling in a JSON
 * dependency for three fields -- [FIELD_SEPARATOR] and [ENTRY_SEPARATOR] are
 * the ASCII unit/record separator control characters, which can never
 * appear in a Riot id or a region name, so there is no escaping to get wrong.
 */
@Singleton
class RecentSearchRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : RecentSearchRepository {

    override fun observeRecentSearches(): Flow<List<RecentSummonerSearch>> =
        dataStore.data.map { prefs -> decode(prefs[KEY_RECENT_SEARCHES]) }

    override suspend fun addRecentSearch(search: RecentSummonerSearch) {
        dataStore.edit { prefs ->
            val current = decode(prefs[KEY_RECENT_SEARCHES])
            val updated = (listOf(search) + current.filterNot {
                it.riotIdName == search.riotIdName &&
                    it.riotIdTagline == search.riotIdTagline &&
                    it.region == search.region
            }).take(MAX_RECENT_SEARCHES)
            prefs[KEY_RECENT_SEARCHES] = encode(updated)
        }
    }

    private fun encode(searches: List<RecentSummonerSearch>): String =
        searches.joinToString(ENTRY_SEPARATOR.toString()) { search ->
            listOf(search.riotIdName, search.riotIdTagline, search.region.name)
                .joinToString(FIELD_SEPARATOR.toString())
        }

    private fun decode(raw: String?): List<RecentSummonerSearch> {
        if (raw.isNullOrEmpty()) return emptyList()
        return raw.split(ENTRY_SEPARATOR).mapNotNull { entry ->
            val fields = entry.split(FIELD_SEPARATOR)
            if (fields.size != 3) return@mapNotNull null
            val region = Region.entries.firstOrNull { it.name == fields[2] } ?: return@mapNotNull null
            RecentSummonerSearch(riotIdName = fields[0], riotIdTagline = fields[1], region = region)
        }
    }

    private companion object {
        val KEY_RECENT_SEARCHES = stringPreferencesKey("recent_summoner_searches")
        const val FIELD_SEPARATOR: Char = ''
        const val ENTRY_SEPARATOR: Char = ''
        const val MAX_RECENT_SEARCHES = 5
    }
}
