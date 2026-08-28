package com.venom7t.lolguide.domain.quiz.model

import com.venom7t.lolguide.domain.champion.model.Champion

/**
 * One quiz round: guess the champion.
 *
 * Only two modes ship. A quote-guess mode was planned but dropped: Data Dragon
 * does not publish voice-line transcripts, so there is no source of truth to
 * generate a "who said this" question from without inventing text, which
 * `AGENTS.md` §1 forbids.
 */
sealed interface QuizQuestion {
    val correctChampion: Champion
    val options: List<Champion>

    /** Guess the champion from one of their ability icons, sans champion name. */
    data class AbilityIconGuess(
        override val correctChampion: Champion,
        override val options: List<Champion>,
        val abilityImageFileName: String,
        val abilityPatchVersion: String,
    ) : QuizQuestion

    /** Guess the champion from a cropped, zoomed region of their splash art. */
    data class SplashCropGuess(
        override val correctChampion: Champion,
        override val options: List<Champion>,
        val skinNum: Int,
        /** Normalized 0f..1f crop offset, so the UI can reproduce the same crop. */
        val cropOffsetX: Float,
        val cropOffsetY: Float,
    ) : QuizQuestion
}

data class QuizRoundResult(
    val question: QuizQuestion,
    val pickedChampionId: String,
) {
    val wasCorrect: Boolean get() = pickedChampionId == question.correctChampion.id
}

/** In-memory only for Phase 3 (see plan) -- not persisted across app restarts. */
data class QuizSessionState(
    val questionsAnswered: Int = 0,
    val correctAnswers: Int = 0,
) {
    val accuracyPercent: Int
        get() = if (questionsAnswered == 0) 0 else (correctAnswers * 100) / questionsAnswered

    fun withResult(wasCorrect: Boolean) = copy(
        questionsAnswered = questionsAnswered + 1,
        correctAnswers = correctAnswers + if (wasCorrect) 1 else 0,
    )
}
