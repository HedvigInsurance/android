package com.hedvig.android.audio.player

import com.hedvig.android.audio.player.data.ProgressPercentage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

actual fun CommonMediaPlayer(dataSourceUrl: String): CommonMediaPlayer = UnavailableMediaPlayer()

/**
 * A player that can never play. Preparing it reports an error to the error listener, so the audio player ends up
 * in its failed state instead of waiting to become ready.
 */
internal class UnavailableMediaPlayer : CommonMediaPlayer {
  private var onError: ((what: Int, extra: Int) -> Boolean)? = null

  override val isPlaying: Boolean = false
  override val duration: Int = 0

  override fun pause() {}

  override fun start() {}

  override fun stop() {}

  override fun release() {}

  override suspend fun seekToPercent(percentage: ProgressPercentage) {}

  override fun hasReachedTheEnd(): Boolean = false

  override fun getProgressPercentage(): ProgressPercentage = ProgressPercentage(0f)

  override fun setOnErrorListener(block: (what: Int, extra: Int) -> Boolean) {
    onError = block
  }

  override fun setOnPreparedListener(block: () -> Unit) {}

  override fun setOnCompletionListener(block: () -> Unit) {}

  override fun prepareAsync() {
    logcat(LogPriority.WARN) { "Audio playback is not available on this platform" }
    onError?.invoke(0, 0)
  }
}
