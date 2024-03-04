package com.hedvig.audio.player.data

import com.hedvig.android.core.common.android.ProgressPercentage

sealed interface AudioPlayerState {
  object Preparing : AudioPlayerState

  object Failed : AudioPlayerState

  data class Ready(
    val readyState: ReadyState,
    val progressPercentage: ProgressPercentage = ProgressPercentage(0f),
  ) : AudioPlayerState {
    sealed interface ReadyState {
      object NotStarted : ReadyState

      object Done : ReadyState

      object Paused : ReadyState

      object Playing : ReadyState

      object Seeking : ReadyState

      val shouldContinuouslyUpdateProgress: Boolean
        get() = this is Playing || this is Seeking

      val isPlayable: Boolean
        get() = this is NotStarted || this is Done || this is Paused

      val isSeekable: Boolean
        get() = this is Playing || this is Seeking
    }

    companion object {
      fun notStarted(): Ready = Ready(ReadyState.NotStarted, ProgressPercentage(0f))

      fun done(): Ready = Ready(ReadyState.Done, ProgressPercentage(1f))
    }
  }

  val isPlayable: Boolean
    get() = this is Ready && readyState.isPlayable

  val isPaused: Boolean
    get() = this is Ready && readyState is Ready.ReadyState.Paused

  val isSeekable: Boolean
    get() = this is Ready && readyState.isSeekable

  val isFailed: Boolean
    get() = this is Failed
}
