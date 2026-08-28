package com.venom7t.lolguide.domain.builds.model

/**
 * A build the user assembled in the Build Simulator and chose to keep,
 * separate from the simulator's own in-memory working state
 * (`BuildSimulatorState` in `:presentation`) -- this is what survives the
 * screen closing, an uninstall-and-reinstall (via Firebase sync), and a
 * later reload back into the simulator.
 *
 * [id] is a locally generated UUID rather than a natural key: unlike
 * favourites (one row per champion id), a champion can have many saved
 * builds, so nothing about the content is unique enough to key on.
 */
data class SavedBuild(
    val id: String,
    val championId: String,
    /**
     * The items actually placed in the build, in slot order, with empty
     * slots dropped rather than stored as null -- unlike the simulator's own
     * fixed six-slot working state, a saved build only needs to remember
     * what was picked, and reloading it re-fills the simulator's slots from
     * the start.
     */
    val itemIds: List<String>,
    val level: Int,
    val savedAtEpochMillis: Long,
)
