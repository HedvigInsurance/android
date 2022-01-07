package com.hedvig.app.feature.claimdetail.data

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.hedvig.app.feature.claimdetail.data.AudioPlayerState.Ready.ReadyState
import d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface AudioPlayerState {
    object Preparing : AudioPlayerState
    object Failed : AudioPlayerState
    data class Ready(
        val readyState: ReadyState,
        val progress: MutableStateFlow<Float> = MutableStateFlow(if (readyState is ReadyState.Done) 1f else 0f)
    ) : AudioPlayerState {

        init {
            GlobalScope.launch {
                progress.collect {
                    d { "Stelios: progress: $it" }
                }
            }
        }

        sealed interface ReadyState {
            object NotStarted : ReadyState
            object Done : ReadyState
            object Paused : ReadyState
            object Playing : ReadyState

            val isPlayable: Boolean
                get() = this is NotStarted || this is Done || this is Paused
        }
    }

    val isPlayable: Boolean
        get() = this is Ready && readyState.isPlayable

    val isPaused: Boolean
        get() = this is Ready && readyState is ReadyState.Paused
}

private const val ONE_SIXTIETH_OF_A_SECOND: Long = 1_000 / 60

class AudioPlayer(
    signedAudioURL: String,
    coroutineScope: CoroutineScope,
) {
    private val _audioPlayerState: MutableStateFlow<AudioPlayerState> = MutableStateFlow(AudioPlayerState.Preparing)
    val audioPlayerState: StateFlow<AudioPlayerState> = _audioPlayerState.asStateFlow()

    private var mediaPlayer: MediaPlayer? = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setDataSource(signedAudioURL)
        setOnErrorListener { _, _, _ ->
            _audioPlayerState.update { AudioPlayerState.Failed }
            true
        }
        setOnPreparedListener { _audioPlayerState.update { AudioPlayerState.Ready(ReadyState.NotStarted) } }
        setOnCompletionListener { _audioPlayerState.update { AudioPlayerState.Ready(ReadyState.Done) } }
        prepareAsync()
    }

    init {
        coroutineScope.launch {
            audioPlayerState
                .collectLatest { audioPlayerState ->
                    if (audioPlayerState !is AudioPlayerState.Ready) return@collectLatest
                    when (audioPlayerState.readyState) {
                        ReadyState.Playing -> {
                            coroutineScope {
                                while (isActive) {
                                    val mediaPlayer = mediaPlayer ?: return@coroutineScope
                                    audioPlayerState.progress.update { mediaPlayer.getProgressPercentage() }
                                    delay(ONE_SIXTIETH_OF_A_SECOND)
                                }
                            }
                        }
                        else -> {
                            audioPlayerState.progress.update { mediaPlayer?.getProgressPercentage() ?: 0f }
                        }
                    }
                }
        }
    }

    fun pause() {
        if (mediaPlayer == null || audioPlayerState.value.isPaused) return
        mediaPlayer?.pause()
        updateAudioPlayerReadyState(ReadyState.Paused)
    }

    fun play() {
        val audioPlayerState = audioPlayerState.value
        if (mediaPlayer == null || audioPlayerState.isPlayable.not()) return
        if (audioPlayerState is AudioPlayerState.Ready && audioPlayerState.readyState is ReadyState.Done) {
            mediaPlayer?.seekTo(0)
        }
        mediaPlayer?.start()
        updateAudioPlayerReadyState(ReadyState.Playing)
    }

    fun cleanupMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun MediaPlayer.getProgressPercentage(): Float {
        return (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }

    private fun updateAudioPlayerReadyState(readyState: ReadyState) {
        _audioPlayerState.update { oldAudioPlayerState ->
            if (oldAudioPlayerState is AudioPlayerState.Ready) {
                oldAudioPlayerState.copy(readyState = readyState)
            } else {
                AudioPlayerState.Ready(readyState)
            }
        }
    }
}
