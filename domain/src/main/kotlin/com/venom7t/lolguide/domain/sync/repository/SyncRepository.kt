package com.venom7t.lolguide.domain.sync.repository

import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats

/**
 * Cross-device sync for the two pieces of user-authored local state Phase 5
 * mirrors: favourited champions and followed summoners. Room stays the read
 * model the UI binds to (AGENTS.md §8, offline-first); this is a sync
 * target the repository pushes to and pulls from, not a replacement.
 *
 * Sync is deliberately push-on-mutation + pull-and-merge-additively at
 * startup, not a full bidirectional CRDT: a pull can only *add* rows locally
 * that are missing, never delete ones present only remotely-stale. Deletions
 * only ever propagate through an explicit push. This is a conscious scope
 * cut (Phase 5 plan) -- correct enough for "my favourites show up on my
 * other device," not a general offline-sync engine.
 */
interface SyncRepository {

    suspend fun pushFavourite(championId: String, isFavourite: Boolean): Result<Unit>

    suspend fun pushFollowedSummoner(summoner: FollowedSummoner): Result<Unit>

    /** Separate from [pushFollowedSummoner] since unfollowing only ever has the puuid on hand. */
    suspend fun pushUnfollowedSummoner(puuid: String): Result<Unit>

    suspend fun pullFavouriteIds(): Result<Set<String>>

    suspend fun pullFollowedSummoners(): Result<List<FollowedSummoner>>

    suspend fun pushSavedBuild(build: SavedBuild): Result<Unit>

    /** Separate from [pushSavedBuild] since deleting only ever has the id on hand. */
    suspend fun pushDeletedSavedBuild(id: String): Result<Unit>

    suspend fun pullSavedBuilds(): Result<List<SavedBuild>>

    /**
     * Lifetime game stats (streaks, win counts) per riddle mode -- the one
     * piece of real progress a player would otherwise lose on reinstall,
     * same failure shape favourites had before Phase 5 (Phase 6 follow-up).
     */
    suspend fun pushGameStats(mode: GameMode, stats: GameStats): Result<Unit>

    /**
     * Only ever adopted when the local record is still at its untouched
     * default (see GameProgressSyncUseCase) -- merging two devices' streaks
     * field-by-field is not something a max-of-counters merge can do
     * correctly (a streak depends on *which* days were won, not just how
     * many), so this restores after data loss rather than reconciling
     * concurrent play.
     */
    suspend fun pullGameStats(): Result<Map<GameMode, GameStats>>
}
