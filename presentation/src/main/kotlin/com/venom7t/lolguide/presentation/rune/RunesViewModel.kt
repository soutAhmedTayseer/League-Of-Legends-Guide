package com.venom7t.lolguide.presentation.rune

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.rune.model.RuneTree
import com.venom7t.lolguide.domain.rune.repository.RuneRepository
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class RunesState(
    val isLoading: Boolean = true,
    val trees: ImmutableList<RuneTree> = persistentListOf(),
    /** Which tree tab is showing. Null once loaded means "first tree". */
    val selectedTreeId: Int? = null,
    val patchVersion: String? = null,
    val error: UiText? = null,
) {
    val selectedTree: RuneTree?
        get() = trees.firstOrNull { it.id == selectedTreeId } ?: trees.firstOrNull()
}

sealed interface RunesEvent {
    data object ScreenOpened : RunesEvent
    data object Retry : RunesEvent
    data class TreeSelected(val treeId: Int) : RunesEvent
}

@HiltViewModel
class RunesViewModel @Inject constructor(
    private val runeRepository: RuneRepository,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(RunesState())
    val state: StateFlow<RunesState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: RunesEvent) {
        when (event) {
            RunesEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }

            RunesEvent.Retry -> load()

            is RunesEvent.TreeSelected -> _state.update { it.copy(selectedTreeId = event.treeId) }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val patch = resolvePatch().getOrElse { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                return@launch
            }

            runeRepository.getRuneTrees(patch.version, locale)
                .onSuccess { trees ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            trees = trees.toImmutableList(),
                            selectedTreeId = it.selectedTreeId ?: trees.firstOrNull()?.id,
                            patchVersion = patch.version,
                            error = null,
                        )
                    }
                }
                .onFailure { throwable ->
                    _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                }
        }
    }
}
