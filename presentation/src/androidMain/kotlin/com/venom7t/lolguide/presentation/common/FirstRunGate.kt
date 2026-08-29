package com.venom7t.lolguide.presentation.common

/**
 * Whether this is the app's first-ever run, decided once by
 * `AppStartViewModel` before any real screen mounts (the same choke point
 * that decides the NavHost's start destination) and read from here by
 * `rememberMinimumVisibleLoading` -- a plain object rather than threading a
 * repository through every screen that shows a loading skeleton.
 *
 * Defaults to `true` (show the full first-run polish) so a screen that
 * somehow reads this before `AppStartViewModel` sets it fails toward the
 * safe, harmless outcome -- an unnecessary couple of seconds of shimmer --
 * rather than silently skipping it.
 */
object FirstRunGate {
    @Volatile
    var isFirstRun: Boolean = true
        private set

    fun setIsFirstRun(value: Boolean) {
        isFirstRun = value
    }
}
