package com.venom7t.lolguide.domain.champion.model

/**
 * One of a champion's skins.
 *
 * @param num the skin index used to build splash and loading art URLs. This is
 *   *not* the same as [id], and using [id] in an image path silently 404s.
 * @param hasChromas whether chromas exist for this skin. Data Dragon ships a
 *   boolean here, not a count, and hosts no chroma images -- so this is
 *   reported as an existence flag rather than a fabricated number.
 */
data class Skin(
    val id: String,
    val num: Int,
    val name: String,
    val hasChromas: Boolean,
) {
    val isBase: Boolean get() = num == 0
}
