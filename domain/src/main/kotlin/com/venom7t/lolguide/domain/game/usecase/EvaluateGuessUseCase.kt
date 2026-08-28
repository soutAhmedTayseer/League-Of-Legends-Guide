package com.venom7t.lolguide.domain.game.usecase

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.DamageType
import com.venom7t.lolguide.domain.champion.model.Difficulty
import com.venom7t.lolguide.domain.game.model.Clue
import com.venom7t.lolguide.domain.game.model.ClueAttribute
import com.venom7t.lolguide.domain.game.model.ClueState
import com.venom7t.lolguide.domain.game.model.GuessResult
import javax.inject.Inject

/**
 * Scores one Classic-mode guess against the answer, producing the five clue
 * columns (Phase 6 plan §Clue attributes). Pure and synchronous -- both
 * champions are already in memory, there is nothing to fetch.
 */
class EvaluateGuessUseCase @Inject constructor() {

    /** Melee/ranged split point on Data Dragon's attackrange, in game units. */
    private val meleeRangeThreshold = 300.0

    operator fun invoke(guess: Champion, answer: Champion): GuessResult {
        val clues = listOf(
            roleClue(guess, answer),
            resourceClue(guess, answer),
            damageTypeClue(guess, answer),
            difficultyClue(guess, answer),
            rangeClue(guess, answer),
        )
        return GuessResult(
            championId = guess.id,
            championName = guess.name,
            imageFileName = guess.imageFileName,
            patchVersion = guess.patchVersion,
            isCorrect = guess.id == answer.id,
            clues = clues,
        )
    }

    private fun roleClue(guess: Champion, answer: Champion): Clue {
        val guessTags = guess.tags.map { it.raw }.toSet()
        val answerTags = answer.tags.map { it.raw }.toSet()
        val state = when {
            guessTags == answerTags -> ClueState.MATCH
            guessTags.intersect(answerTags).isNotEmpty() -> ClueState.PARTIAL
            else -> ClueState.MISS
        }
        return Clue(ClueAttribute.ROLE, state, guess.tags.map { it.raw })
    }

    private fun resourceClue(guess: Champion, answer: Champion): Clue {
        val state = if (guess.partype == answer.partype) ClueState.MATCH else ClueState.MISS
        return Clue(ClueAttribute.RESOURCE, state, listOf(guess.partype))
    }

    private fun damageTypeClue(guess: Champion, answer: Champion): Clue {
        val guessType = DamageType.of(guess.info)
        val answerType = DamageType.of(answer.info)
        val state = if (guessType == answerType) ClueState.MATCH else ClueState.MISS
        return Clue(ClueAttribute.DAMAGE_TYPE, state, listOf(guessType.name))
    }

    private fun difficultyClue(guess: Champion, answer: Champion): Clue {
        val guessDifficulty = Difficulty.of(guess.info.difficulty)
        val answerDifficulty = Difficulty.of(answer.info.difficulty)
        val state = when {
            guessDifficulty == answerDifficulty -> ClueState.MATCH
            guessDifficulty.ordinal < answerDifficulty.ordinal -> ClueState.HIGHER
            else -> ClueState.LOWER
        }
        return Clue(ClueAttribute.DIFFICULTY, state, listOf(guessDifficulty.name))
    }

    private fun rangeClue(guess: Champion, answer: Champion): Clue {
        val guessMelee = guess.stats.attackRange < meleeRangeThreshold
        val answerMelee = answer.stats.attackRange < meleeRangeThreshold
        val state = if (guessMelee == answerMelee) ClueState.MATCH else ClueState.MISS
        val label = if (guessMelee) "Melee" else "Ranged"
        return Clue(ClueAttribute.RANGE, state, listOf(label))
    }
}
