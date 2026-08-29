package com.venom7t.lolguide.presentation.quiz

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.usecase.ObserveChampionsUseCase
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.patch.usecase.ResolvePatchUseCase
import com.venom7t.lolguide.domain.quiz.model.QuizQuestion
import com.venom7t.lolguide.domain.quiz.model.QuizSessionState
import com.venom7t.lolguide.domain.quiz.usecase.GenerateQuizQuestionUseCase
import org.koin.android.annotation.KoinViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class QuizState(
    val isLoading: Boolean = true,
    val question: QuizQuestion? = null,
    val session: QuizSessionState = QuizSessionState(),
    /** Set once an answer is picked, so the UI can reveal right/wrong before advancing. */
    val pickedChampionId: String? = null,
    /** True when the cache is too small to build a question (fewer than 4 champions). */
    val insufficientData: Boolean = false,
)

sealed interface QuizEvent {
    data object ScreenOpened : QuizEvent
    data class AnswerPicked(val championId: String) : QuizEvent
    data object NextQuestionRequested : QuizEvent
}

@KoinViewModel
class QuizViewModel (
    private val observeChampions: ObserveChampionsUseCase,
    private val resolvePatch: ResolvePatchUseCase,
    private val generateQuestion: GenerateQuizQuestionUseCase,
    private val locale: AppLocale,
) : ViewModel() {

    private val _state = MutableStateFlow(QuizState())
    val state: StateFlow<QuizState> = _state.asStateFlow()

    private var hasStarted = false

    fun onEvent(event: QuizEvent) {
        when (event) {
            QuizEvent.ScreenOpened -> {
                if (!hasStarted) {
                    hasStarted = true
                    loadNextQuestion()
                }
            }

            is QuizEvent.AnswerPicked -> {
                if (_state.value.pickedChampionId != null) return // already answered
                val question = _state.value.question ?: return
                val wasCorrect = event.championId == question.correctChampion.id
                _state.update {
                    it.copy(
                        pickedChampionId = event.championId,
                        session = it.session.withResult(wasCorrect),
                    )
                }
            }

            QuizEvent.NextQuestionRequested -> loadNextQuestion()
        }
    }

    private fun loadNextQuestion() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, pickedChampionId = null) }

            val champions: List<Champion> = observeChampions().first()
            val patch = resolvePatch().getOrNull()

            if (champions.size < MIN_CHAMPIONS_FOR_QUESTION || patch == null) {
                _state.update { it.copy(isLoading = false, insufficientData = true) }
                return@launch
            }

            val question = generateQuestion(champions, patch.version, locale)
            _state.update {
                it.copy(
                    isLoading = false,
                    question = question,
                    insufficientData = question == null,
                )
            }
        }
    }

    private companion object {
        const val MIN_CHAMPIONS_FOR_QUESTION = 4
    }
}
