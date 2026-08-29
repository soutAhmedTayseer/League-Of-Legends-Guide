package com.venom7t.lolguide.domain.patch.usecase

import org.koin.core.annotation.Factory

import com.venom7t.lolguide.domain.champion.model.Champion
import com.venom7t.lolguide.domain.champion.model.ChampionStats
import com.venom7t.lolguide.domain.item.model.Item
import com.venom7t.lolguide.domain.patch.model.ChampionChange
import com.venom7t.lolguide.domain.patch.model.ItemChange
import com.venom7t.lolguide.domain.patch.model.PatchDiff
import com.venom7t.lolguide.domain.patch.model.StatDelta
import com.venom7t.lolguide.domain.patch.repository.PreviousPatchSnapshotRepository

/**
 * Builds the "what's new" diff by comparing the current cache against the one
 * retained snapshot of the previous patch.
 *
 * Returns null when there is nothing to diff against -- a first-ever install,
 * or an install that has not yet lived through one patch transition. That is
 * a distinct, expected state, not an error: the UI shows "check back after the
 * next patch" rather than a failure.
 */
@Factory
class ComputePatchDiffUseCase(
    private val snapshots: PreviousPatchSnapshotRepository,
) {

    suspend operator fun invoke(
        currentVersion: String,
        currentChampions: List<Champion>,
        currentItems: List<Item>,
    ): PatchDiff? {
        val (previousChampionVersion, previousChampions) =
            snapshots.getPreviousChampions() ?: return null
        val (previousItemVersion, previousItems) =
            snapshots.getPreviousItems() ?: return null

        // Both snapshots must actually predate the current patch. A snapshot
        // captured on the same version as `currentVersion` (e.g. the app was
        // reopened without a patch change since it was taken) is not "the
        // previous patch" and diffing against it would report phantom changes.
        if (previousChampionVersion == currentVersion || previousItemVersion == currentVersion) {
            return null
        }

        return PatchDiff(
            fromVersion = previousChampionVersion,
            toVersion = currentVersion,
            championChanges = diffChampions(previousChampions, currentChampions),
            itemChanges = diffItems(previousItems, currentItems),
        )
    }

    private fun diffChampions(before: List<Champion>, after: List<Champion>): List<ChampionChange> {
        val beforeById = before.associateBy { it.id }
        val afterById = after.associateBy { it.id }
        val changes = mutableListOf<ChampionChange>()

        for (id in afterById.keys - beforeById.keys) {
            val champion = afterById.getValue(id)
            changes += ChampionChange.Added(champion.id, champion.name)
        }
        for (id in beforeById.keys - afterById.keys) {
            val champion = beforeById.getValue(id)
            changes += ChampionChange.Removed(champion.id, champion.name)
        }
        for (id in beforeById.keys intersect afterById.keys) {
            val previous = beforeById.getValue(id)
            val current = afterById.getValue(id)
            val deltas = statDeltas(previous.stats, current.stats)
            if (deltas.isNotEmpty()) {
                changes += ChampionChange.StatsChanged(current.id, current.name, deltas)
            }
        }

        return changes
    }

    private fun statDeltas(before: ChampionStats, after: ChampionStats): Map<String, StatDelta> {
        val pairs = listOf(
            "hp" to (before.hp to after.hp),
            "armor" to (before.armor to after.armor),
            "spellBlock" to (before.spellBlock to after.spellBlock),
            "attackDamage" to (before.attackDamage to after.attackDamage),
            "attackSpeed" to (before.attackSpeed to after.attackSpeed),
            "moveSpeed" to (before.moveSpeed to after.moveSpeed),
        )
        return pairs
            .filter { (_, values) -> values.first != values.second }
            .associate { (key, values) -> key to StatDelta(values.first, values.second) }
    }

    private fun diffItems(before: List<Item>, after: List<Item>): List<ItemChange> {
        val beforeById = before.associateBy { it.id }
        val afterById = after.associateBy { it.id }
        val changes = mutableListOf<ItemChange>()

        for (id in afterById.keys - beforeById.keys) {
            val item = afterById.getValue(id)
            changes += ItemChange.Added(item.id, item.name)
        }
        for (id in beforeById.keys - afterById.keys) {
            val item = beforeById.getValue(id)
            changes += ItemChange.Removed(item.id, item.name)
        }
        for (id in beforeById.keys intersect afterById.keys) {
            val previous = beforeById.getValue(id)
            val current = afterById.getValue(id)
            if (previous.gold.total != current.gold.total) {
                changes += ItemChange.Repriced(
                    itemId = current.id,
                    itemName = current.name,
                    goldBefore = previous.gold.total,
                    goldAfter = current.gold.total,
                )
            }
        }

        return changes
    }
}
