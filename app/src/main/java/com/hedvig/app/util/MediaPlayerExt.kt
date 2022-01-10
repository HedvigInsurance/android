package com.hedvig.app.util

import android.media.MediaPlayer
import androidx.annotation.FloatRange
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Requires that [MediaPlayer] does not have a [MediaPlayer.OnSeekCompleteListener] registered on it, as it will
 * replace it when this method is called, and set it as null right before this suspending function returns.
 */
suspend fun MediaPlayer.seekToPercent(
    @FloatRange(from = 0.0, to = 1.0) percentage: Float,
) = suspendCancellableCoroutine<Unit> { cont ->
    val callback = MediaPlayer.OnSeekCompleteListener {
        this.setOnSeekCompleteListener(null)
        cont.resume(Unit)
    }
    setOnSeekCompleteListener(callback)
    cont.invokeOnCancellation { setOnSeekCompleteListener(null) }
    val positionToSeekTo = (duration.toFloat() * percentage).toInt()
    if (cont.isActive) {
        seekTo(positionToSeekTo)
    }
}

fun MediaPlayer.hasReachedTheEnd(): Boolean {
    return getProgressPercentage() == 1f
}

@FloatRange(from = 0.0, to = 1.0)
fun MediaPlayer.getProgressPercentage(): Float {
    return (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}
