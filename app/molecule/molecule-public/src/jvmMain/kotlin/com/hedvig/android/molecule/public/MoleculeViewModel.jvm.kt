package com.hedvig.android.molecule.public

import androidx.compose.runtime.MonotonicFrameClock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * [Dispatchers.Main] on the JVM carries no [MonotonicFrameClock], which `RecompositionMode.ContextClock` requires, so
 * one is added here.
 */
internal actual val mainDispatcher: CoroutineContext = Dispatchers.Main + DelayFrameClock

/**
 * Completes each frame request 16 ms after it is made, and produces no frames while nobody asks for one. Every
 * Recomposer waits on its own request, so frames of different ViewModels are not aligned with each other. The wait is a
 * [delay] on the frame's dispatcher, so no UI window needs to exist.
 */
private object DelayFrameClock : MonotonicFrameClock {
  override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
    delay(FRAME_DELAY_MILLIS)
    return onFrame(System.nanoTime())
  }

  private const val FRAME_DELAY_MILLIS = 16L
}
