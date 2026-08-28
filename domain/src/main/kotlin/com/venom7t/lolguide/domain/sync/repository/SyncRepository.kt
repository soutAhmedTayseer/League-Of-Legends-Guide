package com.venom7t.lolguide.domain.sync.repository

import com.venom7t.lolguide.domain.followed.model.FollowedSummoner

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
}
