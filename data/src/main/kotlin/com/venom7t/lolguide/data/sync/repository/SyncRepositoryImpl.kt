package com.venom7t.lolguide.data.sync.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.followed.model.FollowedSummoner
import com.venom7t.lolguide.domain.onboarding.model.Region
import com.venom7t.lolguide.domain.sync.repository.SyncRepository
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore-backed sync for favourites and followed summoners, keyed under
 * `/users/{uid}/...` by the anonymous auth uid.
 *
 * Every method requires a signed-in user (`firebaseAuth.currentUser`); if
 * sign-in has not happened yet, calls fail fast with an explanatory error
 * rather than silently no-oping, so a caller that forgot to run
 * `EnsureSignedInUseCase` first sees why nothing synced.
 */
@Singleton
class SyncRepositoryImpl @Inject constructor(
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
}
