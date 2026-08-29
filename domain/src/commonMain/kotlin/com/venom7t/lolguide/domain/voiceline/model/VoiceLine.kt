package com.venom7t.lolguide.domain.voiceline.model

/**
 * One playable voice line for a champion.
 *
 * There is no transcript: Community Dragon serves audio files with no
 * accompanying text, so [label] is a generic ordinal ("Line 3"), not the
 * words spoken. Inventing a transcript would violate `AGENTS.md` §1.
 */
data class VoiceLine(
    val championId: String,
    val label: String,
    val audioUrl: String,
)

/**
 * Whether a champion's voice lines are actually playable.
 *
 * Community Dragon has no documented, reliable index of which champions have
 * split per-line audio files available (most voice content ships as a single
 * packed sound bank, not individual files) versus which do not. Coverage is
 * therefore discovered per champion at request time and reported honestly as
 * one of these three states, rather than assumed to always work.
 */
sealed interface VoiceLineAvailability {
    data class Available(val lines: List<VoiceLine>) : VoiceLineAvailability
    data object Unavailable : VoiceLineAvailability
}
