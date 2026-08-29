package com.venom7t.lolguide.domain.patch.model

/**
 * Everything that changed between two patches, for the "what's new" screen.
 *
 * Computed from two full cached snapshots rather than a single delta the
 * server hands us -- Riot publishes no diff endpoint -- so every field here is
 * a comparison the app itself derived. Per `AGENTS.md` §1 that makes this a
 * **derived** result: it must always carry both patch labels so the UI can be
 * explicit about what is being compared, and it must never be shown without
 * that context.
 */
data class PatchDiff(
    val fromVersion: String,
    val toVersion: String,
    val championChanges: List<ChampionChange>,
    val itemChanges: List<ItemChange>,
) {
    val isEmpty: Boolean get() = championChanges.isEmpty() && itemChanges.isEmpty()
}

sealed interface ChampionChange {
    val championId: String
    val championName: String

    data class Added(
        override val championId: String,
        override val championName: String,
    ) : ChampionChange

    data class Removed(
        override val championId: String,
        override val championName: String,
    ) : ChampionChange

    /**
     * @param statDeltas non-empty stat fields that changed value, keyed by a
     *   stable field name ("armor", "attackDamage", ...) rather than a
     *   translated label -- `:domain` holds no user-facing text (AGENTS.md §10).
     */
    data class StatsChanged(
        override val championId: String,
        override val championName: String,
        val statDeltas: Map<String, StatDelta>,
    ) : ChampionChange
}

data class StatDelta(val before: Double, val after: Double) {
    val delta: Double get() = after - before
}

sealed interface ItemChange {
    val itemId: String
    val itemName: String

    data class Added(override val itemId: String, override val itemName: String) : ItemChange

    data class Removed(override val itemId: String, override val itemName: String) : ItemChange

    data class Repriced(
        override val itemId: String,
        override val itemName: String,
        val goldBefore: Int,
        val goldAfter: Int,
    ) : ItemChange
}
