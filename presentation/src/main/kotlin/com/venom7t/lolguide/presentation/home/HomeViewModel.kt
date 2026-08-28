package com.venom7t.lolguide.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.domain.patch.usecase.ComputePatchDiffUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeState(
    val patchVersion: String? = null,
    val isPatchStale: Boolean = false,
    /** Null means "not yet checked" or "genuinely nothing to compare against". */
    val hasWhatsNew: Boolean = false,
)

sealed interface HomeEvent {
    data object ScreenOpened : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val resolvePatch: ResolvePatchUseCase,
    private val observeChampions: ObserveChampionsUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val computePatchDiff: ComputePatchDiffUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val patch = resolvePatch().getOrNull() ?: return@launch
            _state.update { it.copy(patchVersion = patch.version, isPatchStale = patch.isStale) }

            val champions = observeChampions().first()
            val items = observeItems().first()
            // Cheap existence check only -- the full diff computation happens
            // in WhatsNewViewModel when the user actually opens that screen.
            // The home teaser only needs to know whether there is anything
            // to show at all.
            val diff = computePatchDiff(patch.version, champions, items)
            _state.update { it.copy(hasWhatsNew = diff != null && !diff.isEmpty) }
        }
    }
}
