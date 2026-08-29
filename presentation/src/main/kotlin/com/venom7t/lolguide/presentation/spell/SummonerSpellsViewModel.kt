package com.venom7t.lolguide.presentation.spell

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.spell.model.SummonerSpell
import com.venom7t.lolguide.domain.spell.repository.SummonerSpellRepository
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import org.koin.android.annotation.KoinViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class SummonerSpellsState(
    val isLoading: Boolean = true,
    /**
     * Already filtered to [SummonerSpell.isSummonersRift] -- Data Dragon ships
     * Arena- and ARAM-only spells in the same payload, and a Rift reference
     * showing those would be actively wrong for the mode most players are in.
     */
    val spells: ImmutableList<SummonerSpell> = persistentListOf(),
    val patchVersion: String? = null,
    val error: UiText? = null,
)

sealed interface SummonerSpellsEvent {
    data object ScreenOpened : SummonerSpellsEvent
    data object Retry : SummonerSpellsEvent
}

@KoinViewModel
class SummonerSpellsViewModel (
    private val spellRepository: SummonerSpellRepository,
    private val resolvePatch: ResolvePatchUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(SummonerSpellsState())
    val state: StateFlow<SummonerSpellsState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: SummonerSpellsEvent) {
        when (event) {
            SummonerSpellsEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    load()
                }
            }

            SummonerSpellsEvent.Retry -> load()
        }
    }

    private fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val patch = resolvePatch().getOrElse { throwable ->
                _state.update { it.copy(isLoading = false, error = throwable.toUiText()) }
                return@launch
            }

            spellRepository.getSummonerSpells(patch.version, locale)
                .onSuccess { spells ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            spells = spells
                                .filter { spell -> spell.isSummonersRift }
                                .sortedBy { spell -> spell.name }
                                .toImmutableList(),
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
