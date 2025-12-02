package com.hedvig.android.audio.player

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.hedvig.audio.player.data.ProgressPercentage
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

actual fun CommonMediaPlayer(dataSourceUrl: String): CommonMediaPlayer {
  val mediaPlayer = MediaPlayer().apply {
    setAudioAttributes(
      AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .build(),
    )
    setDataSource(dataSourceUrl)
  }
  return AndroidMediaPlayer(mediaPlayer)
}

private class AndroidMediaPlayer(
  private val mediaPlayer: MediaPlayer,
) : CommonMediaPlayer {
  override val isPlaying: Boolean
    get() = mediaPlayer.isPlaying

  override fun pause() {
    mediaPlayer.pause()
  }

  override fun start() {
    mediaPlayer.start()
  }

  override fun stop() {
    mediaPlayer.stop()
  }

  override fun release() {
    mediaPlayer.release()
  }

  override suspend fun seekToPercent(percentage: ProgressPercentage) {
    mediaPlayer.seekToPercent(percentage)
  }

  override fun hasReachedTheEnd(): Boolean {
    return mediaPlayer.hasReachedTheEnd()
  }

  override fun getProgressPercentage(): ProgressPercentage {
    return mediaPlayer.getProgressPercentage()
  }

  override fun setOnErrorListener(block: (what: Int, extra: Int) -> Boolean) {
    mediaPlayer.setOnErrorListener { _, what, extra ->
      block(what, extra)
    }
  }

  override fun setOnPreparedListener(block: () -> Unit) {
    mediaPlayer.setOnPreparedListener {
      block()
    }
  }

  override fun setOnCompletionListener(block: () -> Unit) {
    mediaPlayer.setOnCompletionListener {
      block()
    }
  }

  override fun prepareAsync() {
    mediaPlayer.prepareAsync()
  }
}

/**
 * Requires that [MediaPlayer] does not have a [MediaPlayer.OnSeekCompleteListener] registered on it, as it will
 * replace it when this method is called, and set it as null right before this suspending function returns.
 */
private suspend fun MediaPlayer.seekToPercent(percentage: ProgressPercentage) = suspendCancellableCoroutine { cont ->
  val callback = MediaPlayer.OnSeekCompleteListener {
    this.setOnSeekCompleteListener(null)
    cont.resume(Unit)
  }
  setOnSeekCompleteListener(callback)
  cont.invokeOnCancellation { setOnSeekCompleteListener(null) }
  val positionToSeekTo = (duration.toFloat() * percentage.value).toInt()
  if (cont.isActive) {
    seekTo(positionToSeekTo)
  }
}

private fun MediaPlayer.hasReachedTheEnd(): Boolean {
  return getProgressPercentage().isDone
}

private fun MediaPlayer.getProgressPercentage(): ProgressPercentage {
  return ProgressPercentage.safeValue(currentPosition.toFloat() / duration.toFloat())
}
