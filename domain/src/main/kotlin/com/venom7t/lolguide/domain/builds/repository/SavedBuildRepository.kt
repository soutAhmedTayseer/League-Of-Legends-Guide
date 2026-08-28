package com.venom7t.lolguide.domain.builds.repository

import com.venom7t.lolguide.domain.builds.model.SavedBuild
import kotlinx.coroutines.flow.Flow

interface SavedBuildRepository {

    fun observeSavedBuilds(championId: String): Flow<List<SavedBuild>>

    suspend fun getById(id: String): SavedBuild?

    suspend fun saveBuild(
        championId: String,
        itemIds: List<String>,
        level: Int,
    ): Result<SavedBuild>

    suspend fun deleteBuild(id: String): Result<Unit>

    /** Adds [build] locally if not already present -- the pull side of sync's additive merge. */
    suspend fun ensureBuild(build: SavedBuild)
}
