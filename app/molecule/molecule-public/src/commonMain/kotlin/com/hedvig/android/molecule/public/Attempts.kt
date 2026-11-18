@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.molecule.public

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

/** Runs a presenter's request, such as a mutation, once per accepted [start]. Made by [rememberAttempts]. */
@HiddenFromObjC
@Stable
interface Attempts<in R : Any> {
  /** True from an accepted [start] until the run it started returns or is cancelled. */
  val isRunning: Boolean

  /**
   * Runs [request] unless a run is in flight, or, for terminal attempts, one has succeeded. A later start never
   * cancels a run in flight. Returns whether a run was started.
   */
  fun start(request: R): Boolean
}

/**
 * Runs [run] once for each accepted [Attempts.start], in this composition. Every accepted start runs, also when its
 * request equals the last one and both are handled between two frames. [run] returns whether the request succeeded.
 * With [terminal], which is read once, a success ends the attempts: every later start is refused.
 *
 * The run belongs to this composition, so it is cancelled when the presenter stops, and a presenter restarted from its
 * last state starts with no run in flight. A presenter that showed a run in flight decides from its own state what a
 * restart shows for it.
 */
@HiddenFromObjC
@Composable
fun <R : Any> rememberAttempts(terminal: Boolean = false, run: suspend (request: R) -> Boolean): Attempts<R> {
  val attempts = remember { AttemptsState<R>(terminal) }
  val currentRun by rememberUpdatedState(run)
  LaunchedEffect(attempts.number) {
    val request = attempts.request ?: return@LaunchedEffect
    var succeeded = false
    try {
      succeeded = currentRun(request)
    } finally {
      attempts.finish(succeeded)
    }
  }
  return attempts
}

private class AttemptsState<R : Any>(private val terminal: Boolean) : Attempts<R> {
  /** Grows with every accepted start and never goes back, so each one restarts the effect keyed on it. */
  var number by mutableIntStateOf(0)
    private set
  var request: R? by mutableStateOf(null)
    private set
  override var isRunning: Boolean by mutableStateOf(false)
    private set
  private var succeeded by mutableStateOf(false)

  override fun start(request: R): Boolean {
    if (isRunning || (terminal && succeeded)) return false
    this.request = request
    isRunning = true
    number++
    return true
  }

  fun finish(succeeded: Boolean) {
    if (succeeded) this.succeeded = true
    isRunning = false
  }
}
