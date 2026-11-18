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

/** Where the runs of [Attempts] stand. Map it with an exhaustive `when`, so a presenter decides what each case shows. */
@HiddenFromObjC
sealed interface AttemptStatus {
  /** Nothing is running. */
  data object Idle : AttemptStatus

  /** A run started by [Attempts.start] has not returned yet. */
  data object Running : AttemptStatus

  /**
   * The presenter restarted from a state in which a run was in flight. That run was cancelled with the stopped
   * presenter, and its request may have reached the server. Lasts until the next accepted start.
   */
  data object Interrupted : AttemptStatus
}

/** Runs a presenter's request, such as a mutation, once per accepted [start]. Made by [rememberAttempts]. */
@HiddenFromObjC
@Stable
interface Attempts<in R : Any> {
  val status: AttemptStatus

  /**
   * Runs [request] unless a run is in flight, or, for terminal attempts, one has succeeded in this presenter run. A
   * later start never cancels a run in flight. Returns whether a run was started.
   *
   * Call it in the event handler that checked the presenter's guard, with nothing suspending in between: this call is
   * what makes the next event see a run in flight.
   */
  fun start(request: R): Boolean
}

/**
 * Runs [run] once for each accepted [Attempts.start], in this composition. Every accepted start runs, also when its
 * request equals the last one and both are handled between two frames. [run] returns whether the request succeeded.
 *
 * Work that must come before the request, such as storing the input it is built from, belongs inside [run]: a second
 * event that arrives while that work suspends outside [run] still sees nothing running, so it passes the same guard
 * and does the work again.
 *
 * Not for a load that starts by itself when the presenter composes: [run] runs only on [Attempts.start], and
 * [AttemptStatus.Interrupted] means nothing for a read, which simply runs again. Use a `LaunchedEffect` keyed on a
 * number that only grows, such as a reload count, which gives the same "every start runs" property.
 *
 * The run belongs to this composition, so it is cancelled when the presenter stops. Pass [restoredWhileRunning] from
 * the last state: true when that state showed a run in flight, which makes the status start as
 * [AttemptStatus.Interrupted]. With [terminal], which is read once, a success refuses every later start, within this
 * presenter run only: after a restart, only the presenter's own state stops a second run.
 */
@HiddenFromObjC
@Composable
fun <R : Any> rememberAttempts(
  restoredWhileRunning: Boolean,
  terminal: Boolean = false,
  run: suspend (request: R) -> Boolean,
): Attempts<R> {
  val attempts = remember { AttemptsState<R>(terminal, restoredWhileRunning) }
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

private class AttemptsState<R : Any>(private val terminal: Boolean, restoredWhileRunning: Boolean) : Attempts<R> {
  /** Grows with every accepted start and never goes back, so each one restarts the effect keyed on it. */
  var number by mutableIntStateOf(0)
    private set
  var request: R? by mutableStateOf(null)
    private set
  override var status: AttemptStatus by mutableStateOf(
    if (restoredWhileRunning) AttemptStatus.Interrupted else AttemptStatus.Idle,
  )
    private set
  private var succeeded by mutableStateOf(false)

  override fun start(request: R): Boolean {
    if (status == AttemptStatus.Running || (terminal && succeeded)) return false
    this.request = request
    status = AttemptStatus.Running
    number++
    return true
  }

  fun finish(succeeded: Boolean) {
    if (succeeded) this.succeeded = true
    status = AttemptStatus.Idle
  }
}
