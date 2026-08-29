package com.venom7t.lolguide.data.sync.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed sync for favourites and followed summoners, keyed under
 * `/users/{uid}/...` by the anonymous auth uid.
 *
 * Every method requires a signed-in user (`firebaseAuth.currentUser`); if
 * sign-in has not happened yet, calls fail fast with an explanatory error
 * rather than silently no-oping, so a caller that forgot to run
 * `EnsureSignedInUseCase` first sees why nothing synced.
 */
class SyncRepositoryImpl constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : SyncRepository {

    private fun requireUid(): String =
        firebaseAuth.currentUser?.uid ?: error("Sync called before sign-in")

    override suspend fun pushFavourite(championId: String, isFavourite: Boolean): Result<Unit> =
        runCatchingCancellable {
            val doc = firestore.collection("users").document(requireUid())
                .collection("favourites").document(championId)
            if (isFavourite) {
                doc.set(mapOf("championId" to championId)).await()
            } else {
                doc.delete().await()
            }
        }

    override suspend fun pushFollowedSummoner(summoner: FollowedSummoner): Result<Unit> =
        runCatchingCancellable {
            firestore.collection("users").document(requireUid())
                .collection("followedSummoners").document(summoner.puuid)
                .set(
                    mapOf(
                        "puuid" to summoner.puuid,
                        "riotIdName" to summoner.riotIdName,
                        "riotIdTagline" to summoner.riotIdTagline,
                        "region" to summoner.region.name,
                        "followedAtEpochMillis" to summoner.followedAtEpochMillis,
                    ),
                ).await()
            Unit
        }

    override suspend fun pushUnfollowedSummoner(puuid: String): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("followedSummoners").document(puuid)
            .delete().await()
        Unit
    }

    override suspend fun pullFavouriteIds(): Result<Set<String>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("favourites").get().await()
            .documents.mapNotNull { it.getString("championId") }.toSet()
    }

    override suspend fun pullFollowedSummoners(): Result<List<FollowedSummoner>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("followedSummoners").get().await()
            .documents.mapNotNull { doc ->
                val puuid = doc.getString("puuid") ?: return@mapNotNull null
                val name = doc.getString("riotIdName") ?: return@mapNotNull null
                val tagline = doc.getString("riotIdTagline") ?: return@mapNotNull null
                val regionName = doc.getString("region") ?: return@mapNotNull null
                val region = Region.entries.firstOrNull { it.name == regionName } ?: return@mapNotNull null
                val followedAt = doc.getLong("followedAtEpochMillis") ?: 0L
                FollowedSummoner(puuid, name, tagline, region, followedAt)
            }
    }

    override suspend fun pushSavedBuild(build: SavedBuild): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("savedBuilds").document(build.id)
            .set(
                mapOf(
                    "id" to build.id,
                    "championId" to build.championId,
                    "itemIds" to build.itemIds,
                    "level" to build.level,
                    "savedAtEpochMillis" to build.savedAtEpochMillis,
                ),
            ).await()
        Unit
    }

    override suspend fun pushDeletedSavedBuild(id: String): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("savedBuilds").document(id)
            .delete().await()
        Unit
    }

    override suspend fun pullSavedBuilds(): Result<List<SavedBuild>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("savedBuilds").get().await()
            .documents.mapNotNull { doc ->
                val id = doc.getString("id") ?: return@mapNotNull null
                val championId = doc.getString("championId") ?: return@mapNotNull null
                @Suppress("UNCHECKED_CAST")
                val itemIds = doc.get("itemIds") as? List<String> ?: return@mapNotNull null
                val level = doc.getLong("level")?.toInt() ?: return@mapNotNull null
                val savedAt = doc.getLong("savedAtEpochMillis") ?: 0L
                SavedBuild(id, championId, itemIds, level, savedAt)
            }
    }

    override suspend fun pushGameStats(mode: GameMode, stats: GameStats): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("gameStats").document(mode.name)
            .set(
                mapOf(
                    "played" to stats.played,
                    "won" to stats.won,
                    "currentStreak" to stats.currentStreak,
                    "bestStreak" to stats.bestStreak,
                    "lastCompletedEpochDay" to stats.lastCompletedEpochDay,
                ),
            ).await()
        Unit
    }

    override suspend fun pullGameStats(): Result<Map<GameMode, GameStats>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("gameStats").get().await()
            .documents.mapNotNull { doc ->
                val mode = GameMode.entries.firstOrNull { it.name == doc.id } ?: return@mapNotNull null
                val stats = GameStats(
                    mode = mode,
                    played = doc.getLong("played")?.toInt() ?: 0,
                    won = doc.getLong("won")?.toInt() ?: 0,
                    currentStreak = doc.getLong("currentStreak")?.toInt() ?: 0,
                    bestStreak = doc.getLong("bestStreak")?.toInt() ?: 0,
                    lastCompletedEpochDay = doc.getLong("lastCompletedEpochDay"),
                )
                mode to stats
            }
            .toMap()
    }
}
