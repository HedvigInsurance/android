package com.hedvig.app.feature.claimdetail.data

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.annotation.FloatRange
import com.hedvig.app.feature.claimdetail.data.AudioPlayerState.Ready.ReadyState
import d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface AudioPlayerState {
    object Preparing : AudioPlayerState
    object Failed : AudioPlayerState
    data class Ready(
        val readyState: ReadyState,
        val progress: Float = 0f,
    ) : AudioPlayerState {
        sealed interface ReadyState {
            object NotStarted : ReadyState
            object Done : ReadyState
            object Paused : ReadyState
            object Playing : ReadyState
            object Seeking : ReadyState

            val isPlayable: Boolean
                get() = this is NotStarted || this is Done || this is Paused
        }

        companion object {
            fun notStarted(): Ready = Ready(ReadyState.NotStarted, 0f)
            fun done(): Ready = Ready(ReadyState.Done, 1f)
            fun paused(progress: Float = 0f): Ready = Ready(ReadyState.Paused, progress)
            fun playing(progress: Float = 0f): Ready = Ready(ReadyState.Playing, progress)
            fun seeking(progress: Float = 0f): Ready = Ready(ReadyState.Seeking, progress)
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
        setOnPreparedListener { _audioPlayerState.update { AudioPlayerState.Ready.notStarted() } }
        setOnCompletionListener { _audioPlayerState.update { AudioPlayerState.Ready.done() } }
        setOnSeekCompleteListener {
            d { "Stelios ended seek" }
            updateAudioPlayerReadyState(ReadyState.Paused)
        }
        prepareAsync()
    }

    init {
        coroutineScope.launch {
            audioPlayerState
                .map { audioPlayerState: AudioPlayerState -> (audioPlayerState as? AudioPlayerState.Ready)?.readyState }
                .map { readyState: ReadyState? -> readyState as? ReadyState.Playing }
                .distinctUntilChanged()
                .collectLatest { playingReadyState: ReadyState.Playing? ->
                    if (playingReadyState == null) return@collectLatest
                    coroutineScope {
                        while (isActive) {
                            val mediaPlayer = mediaPlayer ?: return@coroutineScope
                            val progress = mediaPlayer.getProgressPercentage()
                            updateAudioPlayerProgressIfIsCurrentlyPlaying(progress)
                            delay(ONE_SIXTIETH_OF_A_SECOND)
                        }
                    }
                }
        }
    }

    fun pausePlayer() {
        if (mediaPlayer == null || audioPlayerState.value.isPaused) return
        mediaPlayer?.pause()
        updateAudioPlayerReadyState(ReadyState.Paused)
    }

    fun startPlayer() {
        val audioPlayerState = audioPlayerState.value
        if (mediaPlayer == null || audioPlayerState.isPlayable.not()) return
        if (audioPlayerState is AudioPlayerState.Ready && audioPlayerState.readyState is ReadyState.Done) {
            mediaPlayer?.seekTo(0)
        }
        mediaPlayer?.start()
        updateAudioPlayerReadyState(ReadyState.Playing)
    }

    fun seekTo(@FloatRange(from = 0.0, to = 1.0) percentage: Float) {
        if (audioPlayerState.value !is AudioPlayerState.Ready) return
        val mediaPlayer = mediaPlayer ?: return
        mediaPlayer.pause()
        _audioPlayerState.update { AudioPlayerState.Ready.seeking(percentage) }
        mediaPlayer.seekTo(percentage)
    }

    fun cleanupMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
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

    private fun updateAudioPlayerProgressIfIsCurrentlyPlaying(progress: Float) {
        _audioPlayerState.update { oldAudioPlayerState ->
            if (oldAudioPlayerState is AudioPlayerState.Ready && oldAudioPlayerState.readyState is ReadyState.Playing) {
                oldAudioPlayerState.copy(progress = progress)
            } else {
                oldAudioPlayerState
            }
        }
    }

    private fun MediaPlayer.seekTo(@FloatRange(from = 0.0, to = 1.0) percentage: Float) {
        val positionToSeekTo = (duration.toFloat() * percentage).toInt()
        seekTo(positionToSeekTo)
    }

    @FloatRange(from = 0.0, to = 1.0)
    private fun MediaPlayer.getProgressPercentage(): Float {
        return (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
    }
}
