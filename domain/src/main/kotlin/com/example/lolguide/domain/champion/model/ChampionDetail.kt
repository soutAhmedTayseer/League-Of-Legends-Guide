package com.example.lolguide.domain.champion.model

/**
 * The abilities half of a champion, fetched separately from the list payload.
 *
 * [patchVersion] must match the [Champion.patchVersion] it is displayed next
 * to. Mixing patches within one screen is a rejection criterion (AGENTS.md §1).
 */
data class ChampionDetail(
    val championId: String,
    val lore: String,
    val passive: Passive,
    val spells: List<Spell>,
    val skins: List<Skin>,
    val patchVersion: String,
)

data class Passive(
    val name: String,
    val description: String,
    val imageFileName: String,
)

/**
 * One of Q/W/E/R.
 *
 * [cooldownPerRank] and [costPerRank] are lists rather than Riot's
 * slash-delimited "14/12/10/8/6" burn strings, so the UI can render a real
 * per-rank table instead of a blob of text. Parsing happens once, in the data
 * layer mapper.
 */
data class Spell(
    val id: String,
    val slot: SpellSlot,
    val name: String,
    val description: String,
    val imageFileName: String,
    val cooldownPerRank: List<Double>,
    val costPerRank: List<Double>,
    val costType: String,
    val maxRank: Int,
)

/** The key a spell is bound to. Data Dragon orders `spells` Q, W, E, R. */
enum class SpellSlot {
    Q, W, E, R;

    companion object {
        /** Maps a position in Data Dragon's `spells` array to its key. */
        fun fromIndex(index: Int): SpellSlot? = entries.getOrNull(index)
    }
}
