package com.venom7t.lolguide.presentation.game.round

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.GetChampionDetailUseCase
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.champion.usecase.SearchChampionsUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.game.model.GameMode
import com.venom7t.lolguide.domain.game.model.RoundProgress
import com.venom7t.lolguide.domain.game.usecase.GiveUpRoundUseCase
import com.venom7t.lolguide.domain.game.usecase.ObserveGameStatsUseCase
import com.venom7t.lolguide.domain.game.usecase.StartOrResumeRoundUseCase
import com.venom7t.lolguide.domain.game.usecase.SubmitGuessUseCase
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.presentation.common.toUiText
import com.venom7t.lolguide.presentation.navigation.GameRoundRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameRoundViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeChampions: ObserveChampionsUseCase,
    private val searchChampions: SearchChampionsUseCase,
    private val startOrResumeRound: StartOrResumeRoundUseCase,
    private val submitGuess: SubmitGuessUseCase,
    private val giveUpRound: GiveUpRoundUseCase,
    private val observeGameStats: ObserveGameStatsUseCase,
    private val getChampionDetail: GetChampionDetailUseCase,
    private val resolvePatch: ResolvePatchUseCase,
) : ViewModel() {

    private val mode = GameMode.fromName(savedStateHandle.toRoute<GameRoundRoute>().mode)

    private val _state = MutableStateFlow(GameRoundState(mode = mode))
    val state: StateFlow<GameRoundState> = _state.asStateFlow()

    private val _effects = Channel<GameRoundEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var hasStarted = false
    private var champions: List<Champion> = emptyList()
    private var round: RoundProgress? = null
    private var answer: Champion? = null

    fun onEvent(event: GameRoundEvent) {
        when (event) {
            GameRoundEvent.ScreenOpened -> start()
            is GameRoundEvent.QueryChanged -> onQueryChanged(event.query)
            is GameRoundEvent.SuggestionSelected -> onGuess(event.champion)
            GameRoundEvent.BackClicked -> viewModelScope.launch {
                _effects.send(GameRoundEffect.NavigateBack)
            }
            GameRoundEvent.PlayAgainDismissed -> Unit

            GameRoundEvent.GiveUpClicked -> _state.update { it.copy(pendingGiveUp = true) }
            GameRoundEvent.GiveUpCancelled -> _state.update { it.copy(pendingGiveUp = false) }
            GameRoundEvent.GiveUpConfirmed -> onGiveUp()
        }
    }

    private fun onGiveUp() {
        val currentRound = round ?: return
        val currentAnswer = answer ?: return
        if (currentRound.isFinished) return

        viewModelScope.launch {
            val updated = giveUpRound(currentRound)
            round = updated
            _state.update {
                it.copy(
                    pendingGiveUp = false,
                    outcome = updated.outcome,
                    revealedAnswerName = currentAnswer.name,
                )
            }
        }
    }

    private fun start() {
        if (hasStarted) return
        hasStarted = true

        observeGameStats(mode)
            .onEach { stats -> _state.update { it.copy(stats = stats) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }

            val patch = resolvePatch().getOrNull()?.version
            champions = observeChampions().first()

            val startedRound = startOrResumeRound(champions, mode)
            if (startedRound == null) {
                _state.update {
                    it.copy(isLoading = false, error = com.venom7t.lolguide.presentation.common.uiText(
                        com.venom7t.lolguide.presentation.R.string.error_no_cached_data,
                    ))
                }
                return@launch
            }
            round = startedRound
            answer = champions.firstOrNull { it.id == startedRound.answerChampionId }

            var abilityIcon: String? = null
            if (mode == GameMode.ABILITY) {
                val championAnswer = answer
                if (championAnswer != null) {
                    getChampionDetail(championAnswer.id, championAnswer.patchVersion, AppLocale.ENGLISH)
                        .onSuccess { result ->
                            // Deterministic per day: same ability every time this
                            // round is reopened, not re-randomised on resume.
                            val spells = result.detail.spells
                            if (spells.isNotEmpty()) {
                                val index = (startedRound.epochDay % spells.size).toInt()
                                abilityIcon = spells[index].imageFileName
                            }
                        }
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    patchVersion = patch,
                    answerChampionId = startedRound.answerChampionId,
                    outcome = startedRound.outcome,
                    abilityIconFileName = abilityIcon,
                    revealedAnswerName = if (startedRound.isFinished) answer?.name else null,
                )
            }
        }
    }

    private fun onQueryChanged(query: String) {
        val already = round?.guessedIds.orEmpty().toSet()
        val suggestions = if (query.isBlank()) {
            emptyList()
        } else {
            searchChampions(champions, query).filter { it.id !in already }.take(SUGGESTION_LIMIT)
        }
        _state.update { it.copy(query = query, suggestions = suggestions) }
    }

    private fun onGuess(champion: Champion) {
        val currentRound = round ?: return
        val currentAnswer = answer ?: return
        if (currentRound.isFinished) return

        viewModelScope.launch {
            val (updatedRound, result) = submitGuess(currentRound, champion, currentAnswer)
            round = updatedRound
            _state.update {
                it.copy(
                    query = "",
                    suggestions = emptyList(),
                    guesses = it.guesses + result,
                    outcome = updatedRound.outcome,
                    revealedAnswerName = if (updatedRound.isFinished) currentAnswer.name else null,
                )
            }
        }
    }

    private companion object {
        const val SUGGESTION_LIMIT = 6
    }
}
