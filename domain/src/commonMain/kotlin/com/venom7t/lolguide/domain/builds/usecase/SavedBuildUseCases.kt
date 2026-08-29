package com.venom7t.lolguide.domain.builds.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.builds.model.SavedBuild
import com.venom7t.lolguide.domain.builds.repository.SavedBuildRepository
import kotlinx.coroutines.flow.Flow

@Factory
class ObserveSavedBuildsUseCase(
    private val repository: SavedBuildRepository,
) {
    operator fun invoke(championId: String): Flow<List<SavedBuild>> =
        repository.observeSavedBuilds(championId)
}

@Factory
class GetSavedBuildUseCase(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(id: String): SavedBuild? = repository.getById(id)
}

@Factory
class SaveBuildUseCase(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(
        championId: String,
        itemIds: List<String>,
        level: Int,
    ): Result<SavedBuild> = repository.saveBuild(championId, itemIds, level)
}

@Factory
class DeleteSavedBuildUseCase(
    private val repository: SavedBuildRepository,
) {
    suspend operator fun invoke(id: String): Result<Unit> = repository.deleteBuild(id)
}
