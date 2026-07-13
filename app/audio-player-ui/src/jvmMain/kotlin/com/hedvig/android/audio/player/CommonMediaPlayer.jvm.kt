package com.hedvig.android.audio.player

import com.hedvig.audio.player.data.ProgressPercentage

/**
 * No JVM/desktop application target ships audio playback — the JVM target exists only for headless
 * Compose render tests and previews. This no-op implementation lets audio-backed composables (e.g.
 * [HedvigAudioPlayer]) compose and render on the JVM without a real media backend. It never plays
 * anything and stays in the initial "preparing" state.
 */
actual fun CommonMediaPlayer(dataSourceUrl: String): CommonMediaPlayer = NoOpCommonMediaPlayer

private object NoOpCommonMediaPlayer : CommonMediaPlayer {
  override val isPlaying: Boolean = false
  override val duration: Int = 0

  override fun pause() {}

  override fun start() {}

  override fun stop() {}

  override fun release() {}

  override suspend fun seekToPercent(percentage: ProgressPercentage) {}

  override fun hasReachedTheEnd(): Boolean = false

  override fun getProgressPercentage(): ProgressPercentage = ProgressPercentage(0f)

  override fun setOnErrorListener(block: (what: Int, extra: Int) -> Boolean) {}

  override fun setOnPreparedListener(block: () -> Unit) {}

  override fun setOnCompletionListener(block: () -> Unit) {}

  override fun prepareAsync() {}
}
