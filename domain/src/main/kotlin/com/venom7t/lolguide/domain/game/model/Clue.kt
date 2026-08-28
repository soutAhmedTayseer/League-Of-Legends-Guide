package com.venom7t.lolguide.domain.game.model

/**
 * How one attribute of a guess compares to the answer.
 *
 * [PARTIAL] exists because roles are a set, not a value: a guess that shares
 * one role out of two is genuinely closer than one that shares none, and
 * flattening that to a miss throws away the most useful signal in the game.
 */
enum class ClueState {
    MATCH,
    PARTIAL,
    MISS,

    /** The answer's value is higher than the guess. Ordered attributes only. */
    HIGHER,

    /** The answer's value is lower than the guess. */
    LOWER,
}

/** Which attribute a clue column reports on. */
enum class ClueAttribute {
    ROLE,
    RESOURCE,
    DAMAGE_TYPE,
    DIFFICULTY,
    RANGE,
}

/**
 * One cell of the clue grid.
 *
 * [displayValues] holds the *guess's* values, not the answer's -- the grid
 * shows what you guessed, coloured by how it scored.
 */
data class Clue(
    val attribute: ClueAttribute,
    val state: ClueState,
    val displayValues: List<String>,
)

/** One complete guess and how every attribute scored. */
data class GuessResult(
    val championId: String,
    val championName: String,
    val imageFileName: String,
    val patchVersion: String,
    val isCorrect: Boolean,
    val clues: List<Clue>,
)
