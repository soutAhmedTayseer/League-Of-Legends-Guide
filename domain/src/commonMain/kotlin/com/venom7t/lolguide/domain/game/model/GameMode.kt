package com.venom7t.lolguide.domain.game.model

/**
 * The three ways to play.
 *
 * A round has no guess limit in any mode -- it ends only on a correct guess
 * or the player giving up. [splashZoomSteps] is not a budget; it is how many
 * wrong guesses it takes [GameMode.SPLASH] to fully reveal the art, after
 * which further wrong guesses just stay at full zoom.
 */
enum class GameMode(val splashZoomSteps: Int = 0) {

    /** Guess the champion; every attempt scores five attributes against it. */
    CLASSIC,

    /** One ability icon, no other context. */
    ABILITY,

    /**
     * A cropped region of splash art that widens with each wrong guess, so a
     * miss buys information rather than only costing an attempt.
     */
    SPLASH(splashZoomSteps = 5),
    ;

    companion object {
        fun fromName(value: String?): GameMode =
            entries.firstOrNull { it.name == value } ?: CLASSIC
    }
}
