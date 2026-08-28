package com.venom7t.lolguide.domain.summoner.usecase

import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.summoner.model.RankedEntry
import com.venom7t.lolguide.domain.summoner.model.Summoner
import com.venom7t.lolguide.domain.summoner.repository.SummonerRepository
import javax.inject.Inject

class SearchSummonerUseCase @Inject constructor(
    private val repository: SummonerRepository,
) {
    /**
     * Accepts either "Name#TAG" or separate name/tagline -- players type the
     * combined form, so parsing it here means every caller does not need to
     * reimplement the split.
     */
    suspend operator fun invoke(riotId: String, region: Region): Result<Summoner> {
        val separatorIndex = riotId.lastIndexOf('#')
        if (separatorIndex <= 0 || separatorIndex == riotId.length - 1) {
            return Result.failure(IllegalArgumentException("Expected Name#TAG, got: $riotId"))
        }
        val name = riotId.substring(0, separatorIndex)
        val tagline = riotId.substring(separatorIndex + 1)
        return repository.searchByRiotId(name, tagline, region)
    }
}

class GetRankedEntriesUseCase @Inject constructor(
    private val repository: SummonerRepository,
) {
    suspend operator fun invoke(summoner: Summoner): Result<List<RankedEntry>> =
        repository.getRankedEntries(summoner)
}
