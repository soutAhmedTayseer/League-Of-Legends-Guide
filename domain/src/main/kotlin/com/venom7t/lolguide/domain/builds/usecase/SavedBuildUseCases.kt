package com.venom7t.lolguide.domain.builds.usecase

import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveSavedBuildsUseCase @Inject constructor(
    private val repository: SavedBuildRepository,
) {
    operator fun invoke(championId: String): Flow<List<SavedBuild>> =
        repository.observeSavedBuilds(championId)
}

class GetSavedBuildUseCase @Inject constructor(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(id: String): SavedBuild? = repository.getById(id)
}

class SaveBuildUseCase @Inject constructor(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(
        championId: String,
        itemIds: List<String>,
        level: Int,
    ): Result<SavedBuild> = repository.saveBuild(championId, itemIds, level)
}

class DeleteSavedBuildUseCase @Inject constructor(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteBuild(id)
}
