package com.venom7t.lolguide.domain.common

/**
 * The languages the app ships in, paired with the locale code Data Dragon
 * expects in its CDN paths.
 *
 * Data Dragon serves localized champion lore, ability text and item
 * descriptions, so translating the app chrome without also switching this
 * would leave an Arabic UI wrapped around English game text (AGENTS.md §10).
 */
enum class AppLocale(val dataDragonCode: String, val languageTag: String) {
    ENGLISH("en_US", "en"),
    ARABIC("ar_AE", "ar"),
    ;

    companion object {
        val DEFAULT: AppLocale = ENGLISH

        /** Resolves a platform language tag, falling back to [DEFAULT]. */
        fun fromLanguageTag(tag: String?): AppLocale =
            entries.firstOrNull { tag?.startsWith(it.languageTag, ignoreCase = true) == true }
                ?: DEFAULT
    }
}
