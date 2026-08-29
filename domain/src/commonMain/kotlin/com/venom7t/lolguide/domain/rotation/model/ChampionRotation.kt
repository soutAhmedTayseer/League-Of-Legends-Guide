package com.venom7t.lolguide.domain.rotation.model

/**
 * The current free-to-play rotation.
 *
 * Fills the placeholder card the Phase 3 home dashboard shipped with
 * (`AGENTS.md` §8.2 -- a keyed feature degrades to a clear "not configured"
 * state until the key exists, which is exactly what that placeholder was).
 *
 * [championIds] are Data Dragon's numeric champion keys (CHAMPION-V3 returns
 * numeric ids, not Data Dragon's string ids), so displaying these alongside
 * champion art requires resolving numeric key to string id via the already-
 * cached champion list (`Champion.key`).
 */
data class ChampionRotation(
    val championIds: List<Int>,
    val newPlayerChampionIds: List<Int>,
    val maxNewPlayerLevel: Int,
)
