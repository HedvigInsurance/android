package com.hedvig.android.audio.player

import com.hedvig.audio.player.data.ProgressPercentage

interface CommonMediaPlayer {
  val isPlaying: Boolean
  fun pause()
  fun start()
  fun stop()
  fun release()
  suspend fun seekToPercent(percentage: ProgressPercentage)
  fun hasReachedTheEnd(): Boolean
  fun getProgressPercentage(): ProgressPercentage

  fun setOnErrorListener(block: (what: Int, extra: Int) -> Boolean)
  fun setOnPreparedListener(block: () -> Unit)
  fun setOnCompletionListener(block: () -> Unit)
  fun prepareAsync()
}

expect fun CommonMediaPlayer(dataSourceUrl: String): CommonMediaPlayer
