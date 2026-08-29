package com.venom7t.lolguide.domain.sync.usecase

import com.venom7t.lolguide.domain.auth.usecase.EnsureSignedInUseCase
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import com.venom7t.lolguide.domain.favourite.repository.FavouritesRepository
import com.venom7t.lolguide.domain.followed.repository.FollowedSummonerRepository
import com.venom7t.lolguide.domain.game.repository.GameProgressRepository
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Pulls remote favourites/followed-summoners and merges them additively into
 * local storage. Called once per app start (Phase 5 plan) -- not on every
 * screen, since pulling is a network round trip and the local list is
 * already the source of truth the UI reads.
 *
 * A failure here (offline, Firestore unreachable) is swallowed rather than
 * surfaced: sync is a background convenience, not something that should
 * block or error the app's normal offline-first startup.
 */
class SyncOnStartUseCase @Inject constructor(
    private val ensureSignedIn: EnsureSignedInUseCase,
    private val syncRepository: SyncRepository,
    private val favouritesRepository: FavouritesRepository,
    private val followedSummonerRepository: FollowedSummonerRepository,
    private val savedBuildRepository: SavedBuildRepository,
    private val gameProgressRepository: GameProgressRepository,
) {
    suspend operator fun invoke() {
        ensureSignedIn().onFailure { return }

        syncRepository.pullFavouriteIds().onSuccess { ids ->
            ids.forEach { championId -> favouritesRepository.ensureFavourite(championId) }
        }

        syncRepository.pullFollowedSummoners().onSuccess { summoners ->
            summoners.forEach { summoner ->
                if (!followedSummonerRepository.isFollowed(summoner.puuid)) {
                    followedSummonerRepository.followRaw(summoner)
                }
            }
        }

        syncRepository.pullSavedBuilds().onSuccess { builds ->
            builds.forEach { build -> savedBuildRepository.ensureBuild(build) }
        }

        // Only restores into a mode whose local record is still untouched
        // (never played on this device/install) -- see SyncRepository's doc
        // comment on why this is a restore, not a merge, of concurrent play.
        syncRepository.pullGameStats().onSuccess { remoteStatsByMode ->
            remoteStatsByMode.forEach { (mode, remoteStats) ->
                val localStats = gameProgressRepository.observeStats(mode).first()
                if (localStats.played == 0) {
                    gameProgressRepository.saveStats(remoteStats)
                }
            }
        }
    }
}
