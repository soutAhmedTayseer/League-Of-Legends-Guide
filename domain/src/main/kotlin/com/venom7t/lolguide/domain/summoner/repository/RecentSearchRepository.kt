package com.venom7t.lolguide.domain.summoner.repository

import com.venom7t.lolguide.domain.summoner.model.RecentSummonerSearch
import kotlinx.coroutines.flow.Flow

/**
 * Recent summoner searches, local to this device only -- there is no
 * account-scoped sync for this, unlike favourites/followed summoners, since
 * it is a convenience list rather than data the user would expect to carry
 * across devices.
 */
interface RecentSearchRepository {
    fun observeRecentSearches(): Flow<List<RecentSummonerSearch>>
    suspend fun addRecentSearch(search: RecentSummonerSearch)
}
