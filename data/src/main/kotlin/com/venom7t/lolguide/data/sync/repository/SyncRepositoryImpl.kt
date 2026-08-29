package com.venom7t.lolguide.data.sync.repository

import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.GameStats
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore

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
                doc.set(mapOf("championId" to championId))
            } else {
                doc.delete()
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
                )
        }

    override suspend fun pushUnfollowedSummoner(puuid: String): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("followedSummoners").document(puuid)
            .delete()
    }

    override suspend fun pullFavouriteIds(): Result<Set<String>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("favourites").get()
            .documents.mapNotNull { it.get<String?>("championId") }.toSet()
    }

    override suspend fun pullFollowedSummoners(): Result<List<FollowedSummoner>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("followedSummoners").get()
            .documents.mapNotNull { doc ->
                val puuid = doc.get<String?>("puuid") ?: return@mapNotNull null
                val name = doc.get<String?>("riotIdName") ?: return@mapNotNull null
                val tagline = doc.get<String?>("riotIdTagline") ?: return@mapNotNull null
                val regionName = doc.get<String?>("region") ?: return@mapNotNull null
                val region = Region.entries.firstOrNull { it.name == regionName } ?: return@mapNotNull null
                val followedAt = doc.get<Long?>("followedAtEpochMillis") ?: 0L
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
            )
    }

    override suspend fun pushDeletedSavedBuild(id: String): Result<Unit> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("savedBuilds").document(id)
            .delete()
    }

    override suspend fun pullSavedBuilds(): Result<List<SavedBuild>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("savedBuilds").get()
            .documents.mapNotNull { doc ->
                val id = doc.get<String?>("id") ?: return@mapNotNull null
                val championId = doc.get<String?>("championId") ?: return@mapNotNull null
                val itemIds = doc.get<List<String>?>("itemIds") ?: return@mapNotNull null
                val level = doc.get<Long?>("level")?.toInt() ?: return@mapNotNull null
                val savedAt = doc.get<Long?>("savedAtEpochMillis") ?: 0L
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
            )
    }

    override suspend fun pullGameStats(): Result<Map<GameMode, GameStats>> = runCatchingCancellable {
        firestore.collection("users").document(requireUid())
            .collection("gameStats").get()
            .documents.mapNotNull { doc ->
                val mode = GameMode.entries.firstOrNull { it.name == doc.id } ?: return@mapNotNull null
                val stats = GameStats(
                    mode = mode,
                    played = doc.get<Long?>("played")?.toInt() ?: 0,
                    won = doc.get<Long?>("won")?.toInt() ?: 0,
                    currentStreak = doc.get<Long?>("currentStreak")?.toInt() ?: 0,
                    bestStreak = doc.get<Long?>("bestStreak")?.toInt() ?: 0,
                    lastCompletedEpochDay = doc.get<Long?>("lastCompletedEpochDay"),
                )
                mode to stats
            }
            .toMap()
    }
}
