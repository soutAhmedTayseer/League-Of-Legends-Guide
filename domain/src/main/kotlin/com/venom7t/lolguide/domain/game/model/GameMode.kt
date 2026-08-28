package com.venom7t.lolguide.domain.game.model

/**
 * The three ways to play.
 *
 * Each mode is a different amount of information per guess, so each gets a
 * different budget. Classic hands back five clue columns every time and can
 * afford six attempts; the picture modes give almost nothing, so a longer
 * round would just be six shots in the dark.
 */
enum class GameMode(val maxGuesses: Int) {

    /** Guess the champion; every attempt scores five attributes against it. */
    CLASSIC(maxGuesses = 6),

    /** One ability icon, no other context. */
    ABILITY(maxGuesses = 4),

    /**
     * A cropped region of splash art that widens with each wrong guess, so a
     * miss buys information rather than only costing an attempt.
     */
    SPLASH(maxGuesses = 4),
    ;

    companion object {
        fun fromName(value: String?): GameMode =
            entries.firstOrNull { it.name == value } ?: CLASSIC
    }
}
