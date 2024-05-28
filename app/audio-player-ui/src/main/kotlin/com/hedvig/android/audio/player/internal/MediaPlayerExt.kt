package com.hedvig.android.audio.player.internal

import android.media.MediaPlayer
import com.hedvig.audio.player.data.ProgressPercentage
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Requires that [MediaPlayer] does not have a [MediaPlayer.OnSeekCompleteListener] registered on it, as it will
 * replace it when this method is called, and set it as null right before this suspending function returns.
 */
internal suspend fun MediaPlayer.seekToPercent(percentage: ProgressPercentage) = suspendCancellableCoroutine { cont ->
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

internal fun MediaPlayer.hasReachedTheEnd(): Boolean {
  return getProgressPercentage().isDone
}

internal fun MediaPlayer.getProgressPercentage(): ProgressPercentage {
  return ProgressPercentage.safeValue(currentPosition.toFloat() / duration.toFloat())
}
