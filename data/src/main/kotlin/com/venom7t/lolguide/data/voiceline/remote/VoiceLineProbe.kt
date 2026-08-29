package com.venom7t.lolguide.data.voiceline.remote

import com.venom7t.lolguide.data.common.di.DataDragonRetrofit
import com.venom7t.lolguide.data.common.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Best-effort discovery of a champion's split voice-line audio files on
 * Community Dragon.
 *
 * **This is explicitly not a documented API.** Community Dragon has no index
 * endpoint listing which champions have individually playable voice files --
 * most voice content ships packed into per-skin sound banks, not as separate
 * files a client can address by URL. The paths probed here
 * (`raw.communitydragon.org/latest/game/assets/sounds/wwise2016/vo/{locale}/
 * characters/{championId}/skin0/{championId}_vo_{n}.ogg`) are the pattern
 * community tooling has observed working for a subset of champions, not a
 * contract Riot or Community Dragon has published or guaranteed.
 *
 * Given that, this probes for a small number of lines with real HTTP HEAD
 * requests and reports back only what it actually confirmed exists. A miss
 * is the expected, common case for most champions, not a bug -- callers must
 * treat [ProbeResult.Unavailable] as a normal outcome, never a failure to
 * retry, per the honesty requirement in `AGENTS.md` §1: no line is ever
 * claimed playable without being confirmed.
 */
class VoiceLineProbe constructor(
    // Reuses the keyless Data Dragon OkHttp client (no auth interceptor) --
    // Community Dragon is the same trust tier as Data Dragon (AGENTS.md §8.1).
    @DataDragonRetrofit private val httpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun probe(
        championId: String,
        maxLines: Int = MAX_LINES_TO_PROBE,
    ): ProbeResult = withContext(ioDispatcher) {
        val lowerId = championId.lowercase()
        val foundUrls = mutableListOf<String>()

        for (index in 0 until maxLines) {
            val url = "$BASE_URL/$lowerId/skin0/${lowerId}_vo_$index.ogg"
            if (exists(url)) {
                foundUrls += url
            } else if (foundUrls.isEmpty() && index == 0) {
                // The very first line missing means this champion is not one
                // of the ones with split audio published under this pattern
                // at all -- stop immediately rather than making
                // maxLines-1 more requests that will also miss.
                return@withContext ProbeResult.Unavailable
            } else {
                // Lines are contiguous when they exist at all; the first gap
                // means we have found everything there is.
                break
            }
        }

        if (foundUrls.isEmpty()) ProbeResult.Unavailable else ProbeResult.Found(foundUrls)
    }

    private fun exists(url: String): Boolean = runCatching {
        val request = Request.Builder().url(url).head().build()
        httpClient.newCall(request).execute().use { it.isSuccessful }
    }.getOrDefault(false)

    sealed interface ProbeResult {
        data class Found(val audioUrls: List<String>) : ProbeResult
        data object Unavailable : ProbeResult
    }

    private companion object {
        const val BASE_URL = "https://raw.communitydragon.org/latest/game/assets/sounds/wwise2016/vo/en_us/characters"
        const val MAX_LINES_TO_PROBE = 8
    }
}
