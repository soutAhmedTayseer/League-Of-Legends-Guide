package com.venom7t.lolguide.presentation.common

/**
 * Builds Data Dragon image URLs.
 *
 * Every function that serves versioned art takes the patch explicitly, so an
 * icon can never be fetched from a different patch than the data beside it
 * (AGENTS.md §1). Splash art is the one genuinely unversioned path on the CDN,
 * and is marked as such.
 */
object DataDragonUrls {

    private const val CDN = "https://ddragon.leagueoflegends.com/cdn"

    /** The square portrait used in the list and the detail header. */
    fun championIcon(version: String, imageFileName: String): String =
        "$CDN/$version/img/champion/$imageFileName"

    /** A spell (Q/W/E/R) icon. */
    fun spellIcon(version: String, imageFileName: String): String =
        "$CDN/$version/img/spell/$imageFileName"

    /** A passive icon. Passives live under their own directory, not `spell`. */
    fun passiveIcon(version: String, imageFileName: String): String =
        "$CDN/$version/img/passive/$imageFileName"

    /**
     * Full-bleed splash art.
     *
     * Deliberately unversioned: this path has no version segment on the CDN,
     * so there is no patch to pass and no patch mismatch to worry about.
     *
     * @param skinNum 0 is the base skin.
     */
    fun championSplash(championId: String, skinNum: Int = 0): String =
        "$CDN/img/champion/splash/${championId}_$skinNum.jpg"

    /** The cropped, wider "loading screen" portrait. Also unversioned. */
    fun championLoading(championId: String, skinNum: Int = 0): String =
        "$CDN/img/champion/loading/${championId}_$skinNum.jpg"

    /** A shop item icon. */
    fun itemIcon(version: String, imageFileName: String): String =
        "$CDN/$version/img/item/$imageFileName"

    /**
     * A rune or rune-tree icon.
     *
     * The one irregular asset path in Data Dragon: runesReforged.json ships a
     * path that is already rooted under cdn/img/ and carries no version
     * segment, so this joins it as-is. Inserting a version here 404s.
     */
    fun runeIcon(iconPath: String): String = "$CDN/img/$iconPath"
}
