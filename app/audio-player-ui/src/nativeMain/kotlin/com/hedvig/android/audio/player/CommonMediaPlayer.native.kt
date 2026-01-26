package com.hedvig.android.audio.player

import com.hedvig.audio.player.data.ProgressPercentage

// todo ios: MediaPlayer
actual fun CommonMediaPlayer(dataSourceUrl: String): CommonMediaPlayer {
  return object : CommonMediaPlayer {
    override val isPlaying: Boolean
      get() = false

    override val duration: Int
      get() = 0

    override fun pause() {
    }

    override fun start() {
    }

    override fun stop() {
    }

    override fun release() {
    }

    override suspend fun seekToPercent(percentage: ProgressPercentage) {
    }

    override fun hasReachedTheEnd(): Boolean {
      return true
    }

    override fun getProgressPercentage(): ProgressPercentage {
      return ProgressPercentage(0f)
    }

    override fun setOnErrorListener(block: (what: Int, extra: Int) -> Boolean) {
    }

    override fun setOnPreparedListener(block: () -> Unit) {
    }

    override fun setOnCompletionListener(block: () -> Unit) {
    }

    override fun prepareAsync() {
    }
  }
}
