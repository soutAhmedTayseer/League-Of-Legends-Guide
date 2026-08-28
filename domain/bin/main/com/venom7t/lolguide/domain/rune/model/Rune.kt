package com.venom7t.lolguide.domain.rune.model

/**
 * A rune tree (Precision, Domination, Sorcery, Resolve, Inspiration).
 *
 * @param iconPath Data Dragon serves rune art from a path that is **already
 *   rooted at `cdn/img/`** and carries no version segment, unlike every other
 *   asset in the CDN. Prefixing it with a patch version produces a 404, so it
 *   is stored verbatim and joined without one.
 */
data class RuneTree(
    val id: Int,
    val key: String,
    val name: String,
    val iconPath: String,
    val slots: List<RuneSlot>,
) {
    /** The first slot holds the keystones -- the tree's headline choices. */
    val keystones: List<Rune> get() = slots.firstOrNull()?.runes.orEmpty()

    /** Everything below the keystone row. */
    val minorSlots: List<RuneSlot> get() = slots.drop(1)
}

/** One row of mutually exclusive choices within a tree. */
data class RuneSlot(
    val runes: List<Rune>,
)

data class Rune(
    val id: Int,
    val key: String,
    val name: String,
    val iconPath: String,
    val shortDescription: String,
    val longDescription: String,
)

/**
 * A rune page the user has assembled.
 *
 * League's rules are encoded here rather than left to the UI: a page is a
 * primary tree with a keystone plus three minor runes, and a secondary tree
 * contributing exactly two, which cannot be the same tree as the primary.
 * [isComplete] is what the UI gates saving on.
 */
data class RunePage(
    val name: String = "",
    val primaryTreeId: Int? = null,
    val keystoneId: Int? = null,
    val primaryRuneIds: List<Int> = emptyList(),
    val secondaryTreeId: Int? = null,
    val secondaryRuneIds: List<Int> = emptyList(),
) {
    val isComplete: Boolean
        get() = primaryTreeId != null &&
            keystoneId != null &&
            primaryRuneIds.size == PRIMARY_MINOR_COUNT &&
            secondaryTreeId != null &&
            secondaryTreeId != primaryTreeId &&
            secondaryRuneIds.size == SECONDARY_COUNT

    companion object {
        const val PRIMARY_MINOR_COUNT = 3
        const val SECONDARY_COUNT = 2
    }
}
