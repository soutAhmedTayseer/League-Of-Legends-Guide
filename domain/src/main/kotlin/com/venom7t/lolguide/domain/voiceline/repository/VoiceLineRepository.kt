package com.venom7t.lolguide.domain.voiceline.repository

import com.venom7t.lolguide.domain.voiceline.model.VoiceLineAvailability

/**
 * Looks up a champion's voice lines, best-effort (see [VoiceLineAvailability]).
 *
 * Deliberately returns a [Result] wrapping an [VoiceLineAvailability] rather
 * than just the availability: a failed lookup (network error) and a
 * confirmed-absent champion (server answered, no lines found) are different
 * things and must be distinguishable so the UI can offer retry only for the
 * former (AGENTS.md §7.2, §13).
 */
interface VoiceLineRepository {
    suspend fun getVoiceLines(championId: String): Result<VoiceLineAvailability>
}
