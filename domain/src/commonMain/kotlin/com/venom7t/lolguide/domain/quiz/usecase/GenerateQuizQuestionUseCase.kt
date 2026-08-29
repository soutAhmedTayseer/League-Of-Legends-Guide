package com.venom7t.lolguide.domain.quiz.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.repository.ChampionRepository
import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.quiz.model.QuizQuestion
import kotlin.random.Random

private const val OPTION_COUNT = 4

/**
 * Builds one random quiz question from the cached champion pool.
 *
 * Requires at least [OPTION_COUNT] cached champions -- a multiple-choice
 * question needs wrong answers to choose from. Returns null rather than a
 * malformed question when the cache is too small (e.g. right after a cold
 * start, before the champion list has finished its first load).
 */
@Factory
class GenerateQuizQuestionUseCase(
    private val championRepository: ChampionRepository,
) {

    suspend operator fun invoke(
        champions: List<Champion>,
        version: String,
        locale: AppLocale,
        random: Random = Random.Default,
    ): QuizQuestion? {
        if (champions.size < OPTION_COUNT) return null

        val correct = champions[random.nextInt(champions.size)]
        val options = buildOptions(correct, champions, random)

        return if (random.nextBoolean()) {
            abilityIconQuestion(correct, options, version, locale)
        } else {
            splashCropQuestion(correct, options, random)
        }
    }

    private fun buildOptions(
        correct: Champion,
        pool: List<Champion>,
        random: Random,
    ): List<Champion> {
        val distractors = pool.filter { it.id != correct.id }
            .shuffled(random)
            .take(OPTION_COUNT - 1)
        return (distractors + correct).shuffled(random)
    }

    private suspend fun abilityIconQuestion(
        correct: Champion,
        options: List<Champion>,
        version: String,
        locale: AppLocale,
    ): QuizQuestion.AbilityIconGuess? {
        val detail = championRepository.getChampionDetail(correct.id, version, locale)
            .getOrNull() ?: return null
        val spell = detail.spells.randomOrNull() ?: return null

        return QuizQuestion.AbilityIconGuess(
            correctChampion = correct,
            options = options,
            abilityImageFileName = spell.imageFileName,
            abilityPatchVersion = detail.patchVersion,
        )
    }

    private fun splashCropQuestion(
        correct: Champion,
        options: List<Champion>,
        random: Random,
    ): QuizQuestion.SplashCropGuess = QuizQuestion.SplashCropGuess(
        correctChampion = correct,
        options = options,
        skinNum = 0,
        // Keep the crop away from the very edges so it reliably lands on
        // the character rather than empty background.
        cropOffsetX = 0.2f + random.nextFloat() * 0.6f,
        cropOffsetY = 0.1f + random.nextFloat() * 0.5f,
    )
}
