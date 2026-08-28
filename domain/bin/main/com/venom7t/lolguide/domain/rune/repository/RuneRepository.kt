package com.venom7t.lolguide.domain.rune.repository

import com.venom7t.lolguide.domain.common.AppLocale
import com.venom7t.lolguide.domain.rune.model.RuneTree

/**
 * Runes are a single small payload of five trees, fetched on demand rather
 * than cached in Room: the whole document is a few kilobytes, it changes only
 * on a patch, and giving it its own table would cost a schema migration for no
 * practical gain over the HTTP cache.
 */
interface RuneRepository {
    suspend fun getRuneTrees(version: String, locale: AppLocale): Result<List<RuneTree>>
}
