package com.hedvig.app.feature.claimdetail

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import d
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed class AudioPlayerState {
    object Preparing : AudioPlayerState()
    object Failed : AudioPlayerState()
    sealed class Ready : AudioPlayerState() {
        var progress: Float by mutableStateOf(0f)

        object Initial : Ready()
        object Done : Ready()
        object Paused : Ready()
        object Playing : Ready()
    }

    val isPlayable: Boolean
        get() = this is Ready.Initial || this is Ready.Done || this is Ready.Paused
}

class AudioPlayer(
    signedAudioURL: String,
    coroutineScope: CoroutineScope,
) {
    var audioPlayerState: AudioPlayerState by mutableStateOf(AudioPlayerState.Preparing)
    var mediaPlayer: MediaPlayer? = MediaPlayer().apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setDataSource(signedAudioURL)
        setOnErrorListener { _, _, _ ->
            audioPlayerState = AudioPlayerState.Failed
            true
        }
        setOnPreparedListener { audioPlayerState = AudioPlayerState.Ready.Initial }
        setOnCompletionListener { audioPlayerState = AudioPlayerState.Ready.Done }
        prepareAsync()
    }

    init {
        coroutineScope.launch {
            while (isActive) {
                val mediaPlayer = mediaPlayer
                val audioPlayerState = audioPlayerState
                if (mediaPlayer != null && audioPlayerState is AudioPlayerState.Ready) {
                    d { "Stelios: State: $audioPlayerState" }
                    val newProgress = (mediaPlayer.currentPosition.toFloat() / mediaPlayer.duration).coerceAtLeast(0f)
                    audioPlayerState.progress = newProgress
                }
                delay(1_000 / 60)
            }
        }
    }

    fun pause() {
        if (mediaPlayer == null || audioPlayerState !is AudioPlayerState.Ready.Playing) return
        mediaPlayer?.pause()
        audioPlayerState = AudioPlayerState.Ready.Paused
    }

    fun play() {
        if (mediaPlayer == null || audioPlayerState.isPlayable.not()) return
        if (audioPlayerState is AudioPlayerState.Ready.Done) {
            mediaPlayer?.seekTo(0)
        }
        mediaPlayer?.start()
        audioPlayerState = AudioPlayerState.Ready.Playing
    }
}
