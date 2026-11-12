package com.hedvig.android.molecule.public

import androidx.compose.runtime.MonotonicFrameClock
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

/**
 * [Dispatchers.Main] on the JVM carries no [MonotonicFrameClock], which `RecompositionMode.ContextClock` requires, so
 * one is added here.
 */
internal actual val mainDispatcher: CoroutineContext = Dispatchers.Main + FixedRateFrameClock

/**
 * Hands out a frame every 16 ms, close to the 60 Hz cadence the Android and iOS display clocks give. The wait is a
 * [delay], so it runs on the frame's dispatcher (the Swing timer in a desktop app, virtual time under a test
 * dispatcher) and needs no UI window to exist.
 */
private object FixedRateFrameClock : MonotonicFrameClock {
  override suspend fun <R> withFrameNanos(onFrame: (frameTimeNanos: Long) -> R): R {
    delay(FRAME_DURATION_MILLIS)
    return onFrame(System.nanoTime())
  }

  private const val FRAME_DURATION_MILLIS = 16L
}
