package com.venom7t.lolguide.data.voiceline.repository

import com.venom7t.lolguide.data.voiceline.remote.VoiceLineProbe
import com.venom7t.lolguide.domain.common.runCatchingCancellable
import com.venom7t.lolguide.domain.voiceline.model.VoiceLine
import com.venom7t.lolguide.domain.voiceline.model.VoiceLineAvailability
import com.venom7t.lolguide.domain.voiceline.repository.VoiceLineRepository

class VoiceLineRepositoryImpl constructor(
    private val probe: VoiceLineProbe,
) : VoiceLineRepository {

    override suspend fun getVoiceLines(championId: String): Result<VoiceLineAvailability> =
        runCatchingCancellable {
            when (val result = probe.probe(championId)) {
                VoiceLineProbe.ProbeResult.Unavailable -> VoiceLineAvailability.Unavailable

                is VoiceLineProbe.ProbeResult.Found -> VoiceLineAvailability.Available(
                    lines = result.audioUrls.mapIndexed { index, url ->
                        VoiceLine(
                            championId = championId,
                            // "Line N", not a transcript -- see VoiceLine's
                            // doc comment on why there is no spoken text here.
                            label = (index + 1).toString(),
                            audioUrl = url,
                        )
                    },
                )
            }
        }
}
