package com.venom7t.lolguide.presentation.whatsnew

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.item.usecase.ObservePurchasableItemsUseCase
import com.venom7t.lolguide.domain.patch.model.PatchDiff
import com.venom7t.lolguide.domain.patch.usecase.ComputePatchDiffUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class WhatsNewState(
    val isLoading: Boolean = true,
    val diff: PatchDiff? = null,
)

sealed interface WhatsNewEvent {
    data object ScreenOpened : WhatsNewEvent
}

@KoinViewModel
class WhatsNewViewModel (
    private val resolvePatch: ResolvePatchUseCase,
    private val observeChampions: ObserveChampionsUseCase,
    private val observeItems: ObservePurchasableItemsUseCase,
    private val computePatchDiff: ComputePatchDiffUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(WhatsNewState())
    val state: StateFlow<WhatsNewState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: WhatsNewEvent) {
        when (event) {
            WhatsNewEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            val patch = resolvePatch().getOrNull()
            if (patch == null) {
                _state.update { it.copy(isLoading = false, diff = null) }
                return@launch
            }
            val champions = observeChampions().first()
            val items = observeItems().first()
            val diff = computePatchDiff(patch.version, champions, items)
            _state.update { it.copy(isLoading = false, diff = diff) }
        }
    }
}
