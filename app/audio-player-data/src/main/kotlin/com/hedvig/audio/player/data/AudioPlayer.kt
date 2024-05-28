package com.hedvig.audio.player.data

import java.io.Closeable
import kotlinx.coroutines.flow.StateFlow

interface AudioPlayer : Closeable {
  val audioPlayerState: StateFlow<AudioPlayerState>

  fun initialize()

  fun startPlayer()

  fun pausePlayer()

  fun retryLoadingAudio()

  fun seekTo(progressPercentage: ProgressPercentage)
}

sealed interface PlayableAudioSource {
  val dataSourceUrl: String

  data class RemoteUrl(val signedAudioUrl: SignedAudioUrl) : PlayableAudioSource {
    override val dataSourceUrl: String = signedAudioUrl.rawUrl
  }

  data class LocalFilePath(val audioFilePath: String) : PlayableAudioSource {
    override val dataSourceUrl: String = audioFilePath
  }
}
