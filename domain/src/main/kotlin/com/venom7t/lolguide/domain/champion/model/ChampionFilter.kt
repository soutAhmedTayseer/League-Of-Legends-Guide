package com.venom7t.lolguide.domain.champion.model

/**
 * List filters, all derived from data already cached, so applying one costs no
 * network request.
 */
data class ChampionFilter(
    val roles: Set<ChampionTag> = emptySet(),
    val resources: Set<String> = emptySet(),
    val difficulties: Set<Difficulty> = emptySet(),
    val damageTypes: Set<DamageType> = emptySet(),
    val favouritesOnly: Boolean = false,
) {
    val isActive: Boolean
        get() = roles.isNotEmpty() || resources.isNotEmpty() ||
            difficulties.isNotEmpty() || damageTypes.isNotEmpty() || favouritesOnly

    val activeCount: Int
        get() = roles.size + resources.size + difficulties.size + damageTypes.size +
            if (favouritesOnly) 1 else 0
}

/** Buckets over Riot's 0-10 `info.difficulty` bar. */
enum class Difficulty(val range: IntRange) {
    LOW(0..3),
    MEDIUM(4..7),
    HIGH(8..10),
    ;

    companion object {
        fun of(difficulty: Int): Difficulty =
            entries.firstOrNull { difficulty in it.range } ?: MEDIUM
    }
}

/**
 * Physical / magic / hybrid.
 *
 * **Inferred, not authoritative.** Riot publishes no damage-type field; this
 * compares the `info.attack` and `info.magic` bars, which are themselves rough
 * marketing figures. The UI must present this as an approximation
 * (`AGENTS.md` §1 forbids passing a guess off as source data).
 */
enum class DamageType {
    PHYSICAL,
    MAGIC,
    HYBRID,
    ;

    companion object {
        /** A gap of 2 or less on a 0-10 scale is too close to call. */
        private const val HYBRID_THRESHOLD = 2

        fun of(info: ChampionInfo): DamageType {
            val difference = info.attack - info.magic
            return when {
                difference > HYBRID_THRESHOLD -> PHYSICAL
                difference < -HYBRID_THRESHOLD -> MAGIC
                else -> HYBRID
            }
        }
    }
}
