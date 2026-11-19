package com.hedvig.android.iosdata.bridge

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

/**
 * At most one call per key at a time: a call with the key of one in flight waits for that one. The call is cancelled
 * when its last waiter leaves, by its own cancel or by its timeout; a waiter's timeout throws [CallTimedOut]. A call that
 * does not end on that cancel, because it is suspended in a Swift-implemented suspend function, which Kotlin's cancel
 * does not reach, keeps its key as stuck: until it ends, a caller of that key gets [CallStuck] at once instead of
 * starting another call that could stick too. So at most one coroutine per key is ever stuck.
 *
 * The lock is recursive because a call of a cancelled scope completes, and runs its completion handler, inside [await].
 */
internal class SingleFlight<K : Any, V>(
  /** Whether a key is still worth keeping as stuck; a key that is not is never marked stuck. */
  private val stillCurrent: (K) -> Boolean = { true },
) {
  private val lock = reentrantLock()
  private val inFlight = mutableMapOf<K, Entry<V>>()
  private val stuck = mutableSetOf<K>()

  private class Entry<V>(val call: Deferred<V>) {
    var waiters = 0
  }

  suspend fun await(key: K, scope: CoroutineScope, timeout: Duration, block: suspend () -> V): V {
    val entry = locked {
      // A finished call's completion handler can run after its waiters already saw the result, so drop it here too;
      // joining it would hand this caller that old result or failure instead of running the call again.
      if (inFlight[key]?.call?.isCompleted == true) {
        inFlight.remove(key)
        stuck.remove(key)
      }
      if (key in stuck) throw CallStuck()
      val entry = inFlight[key] ?: Entry(scope.async(start = CoroutineStart.LAZY) { block() }).also { entry ->
        inFlight[key] = entry
        entry.call.invokeOnCompletion {
          locked {
            if (inFlight[key] === entry) {
              inFlight.remove(key)
              stuck.remove(key)
            }
          }
        }
        entry.call.start()
      }
      entry.waiters++
      entry
    }
    var left = false
    try {
      return awaitWithin(entry.call, timeout) {
        left = true
        leave(key, entry)
      }
    } finally {
      if (!left) withContext(NonCancellable) { leave(key, entry) }
    }
  }

  /** Forgets the stuck keys [predicate] matches; their coroutines stay suspended where Swift left them. */
  fun forgetStuck(predicate: (K) -> Boolean) {
    locked { stuck.removeAll(predicate) }
  }

  /** How many keys are stuck now. */
  fun stuckCount(): Int = locked { stuck.size }

  /** How many callers wait on calls in flight now. */
  fun waiterCount(): Int = locked { inFlight.values.sumOf { it.waiters } }

  /** One waiter left; the last one cancels the call, which counts as stuck if it does not end within a moment. */
  private suspend fun leave(key: K, entry: Entry<V>) {
    val last = locked { --entry.waiters == 0 && !entry.call.isCompleted }
    if (!last) return
    entry.call.cancel()
    withTimeoutOrNull(CANCEL_GRACE) { entry.call.join() }
    locked {
      if (inFlight[key] === entry && !entry.call.isCompleted && entry.waiters == 0 && stillCurrent(key)) stuck += key
    }
  }

  private inline fun <T> locked(block: () -> T): T = lock.withLock(block)
}

/** How long a cancelled call may take to end before it counts as stuck. */
private val CANCEL_GRACE = 200.milliseconds

/** A caller stopped waiting after its timeout; the work may still be running. Not a cancellation. */
internal open class CallTimedOut(message: String = "The call did not finish in time") : Exception(message)

/** A caller was refused at once because an equal call is stuck waiting on a Swift input. */
internal class CallStuck : CallTimedOut("An equal call is stuck")

/** Waits for [call] at most [timeout]; on timeout runs [onTimeout] and throws [CallTimedOut]. */
private suspend fun <T> awaitWithin(call: Deferred<T>, timeout: Duration, onTimeout: suspend () -> Unit): T {
  val result = withTimeoutOrNull(timeout) { Result.success(call.await()) }
  if (result == null) {
    onTimeout()
    throw CallTimedOut()
  }
  return result.getOrThrow()
}
