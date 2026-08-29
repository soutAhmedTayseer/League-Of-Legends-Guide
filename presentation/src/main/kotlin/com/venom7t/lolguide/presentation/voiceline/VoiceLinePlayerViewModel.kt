package com.venom7t.lolguide.presentation.voiceline

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.venom7t.lolguide.domain.voiceline.model.VoiceLine
import com.venom7t.lolguide.domain.voiceline.model.VoiceLineAvailability
import com.venom7t.lolguide.domain.voiceline.repository.VoiceLineRepository
import com.venom7t.lolguide.presentation.common.UiText
import com.venom7t.lolguide.presentation.common.toUiText
import org.koin.android.annotation.KoinViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class VoiceLinePlayerState(
    val isLoading: Boolean = true,
    val lines: ImmutableList<VoiceLine> = persistentListOf(),
    val isAvailable: Boolean = false,
    val playingLineIndex: Int? = null,
    val error: UiText? = null,
)

sealed interface VoiceLinePlayerEvent {
    data class Requested(val championId: String) : VoiceLinePlayerEvent
    data class LineClicked(val index: Int) : VoiceLinePlayerEvent
    data object StopClicked : VoiceLinePlayerEvent
}

/**
 * Owns a single [ExoPlayer] instance for the champion detail screen's voice
 * line panel. Released in [onCleared] -- an undisposed player leaks the
 * underlying media codec resources for the lifetime of the process.
 */
@KoinViewModel
class VoiceLinePlayerViewModel (
    context: Context,
    private val voiceLineRepository: VoiceLineRepository,
) : ViewModel() {

    private val player = ExoPlayer.Builder(context).build().apply {
        addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    _state.update { it.copy(playingLineIndex = null) }
                }
            }
        })
    }

    private val _state = MutableStateFlow(VoiceLinePlayerState())
    val state: StateFlow<VoiceLinePlayerState> = _state.asStateFlow()

    private var loadedForChampionId: String? = null

    fun onEvent(event: VoiceLinePlayerEvent) {
        when (event) {
            is VoiceLinePlayerEvent.Requested -> load(event.championId)
            is VoiceLinePlayerEvent.LineClicked -> play(event.index)
            VoiceLinePlayerEvent.StopClicked -> stop()
        }
    }

    private fun load(championId: String) {
        if (loadedForChampionId == championId) return
        loadedForChampionId = championId

        viewModelScope.launch {
            _state.update { VoiceLinePlayerState(isLoading = true) }

            voiceLineRepository.getVoiceLines(championId)
                .onSuccess { availability ->
                    _state.update {
                        when (availability) {
                            is VoiceLineAvailability.Available -> it.copy(
                                isLoading = false,
                                lines = availability.lines.toImmutableList(),
                                isAvailable = true,
                            )
                            VoiceLineAvailability.Unavailable -> it.copy(
                                isLoading = false,
                                lines = persistentListOf(),
                                isAvailable = false,
                            )
                        }
                    }
                }
                .onFailure { throwable ->
                    _state.update {
                        it.copy(isLoading = false, isAvailable = false, error = throwable.toUiText())
                    }
                }
        }
    }

    private fun play(index: Int) {
        val line = _state.value.lines.getOrNull(index) ?: return
        player.setMediaItem(MediaItem.fromUri(line.audioUrl))
        player.prepare()
        player.play()
        _state.update { it.copy(playingLineIndex = index) }
    }

    private fun stop() {
        player.stop()
        _state.update { it.copy(playingLineIndex = null) }
    }

    override fun onCleared() {
        player.release()
        super.onCleared()
    }
}
