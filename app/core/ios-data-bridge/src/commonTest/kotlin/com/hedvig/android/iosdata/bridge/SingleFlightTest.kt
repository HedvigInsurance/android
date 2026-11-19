@file:OptIn(ExperimentalAtomicApi::class)

package com.hedvig.android.iosdata.bridge

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.suspendCoroutine
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * A block that never resumes and ignores cancellation is what a Swift-implemented suspend function that never calls
 * back looks like from Kotlin (`kit/swift-suspend-cancel-probe/RESULT.txt`): `suspendCoroutine` without a resume.
 */
private suspend fun neverResumesIgnoringCancel(): Int = suspendCoroutine { }

class SingleFlightTest {
  private val scope = CoroutineScope(SupervisorJob())

  @Test
  fun `an equal call while one is in flight joins it`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    val gate = CompletableDeferred<Int>()
    val runs = AtomicInt(0)

    val first = scope.async {
      flight.await("key", scope, 1.minutes) {
        runs.incrementAndFetch()
        gate.await()
      }
    }
    val second = scope.async {
      flight.await("key", scope, 1.minutes) {
        runs.incrementAndFetch()
        gate.await()
      }
    }
    // Both callers wait before the gate opens, so the second can only join the first.
    waitUntil { flight.waiterCount() == 2 }
    gate.complete(7)

    assertThat(first.await()).isEqualTo(7)
    assertThat(second.await()).isEqualTo(7)
    assertThat(runs.load()).isEqualTo(1)
  }

  @Test
  fun `a call after a finished one runs again`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    var runs = 0
    flight.await("key", scope, 1.minutes) { ++runs }
    flight.await("key", scope, 1.minutes) { ++runs }

    assertThat(runs).isEqualTo(2)
  }

  @Test
  fun `a failure reaches the caller and the next call runs again`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    assertFailure { flight.await("key", scope, 1.minutes) { error("boom") } }

    assertThat(flight.await("key", scope, 1.minutes) { 3 }).isEqualTo(3)
  }

  @Test
  fun `a call that never resumes holds its key as stuck and later callers fail at once without a new call`() =
    runBlocking<Unit> {
      val flight = SingleFlight<String, Int>()
      val runs = AtomicInt(0)

      assertFailure {
        flight.await("key", scope, 50.milliseconds) {
          runs.incrementAndFetch()
          neverResumesIgnoringCancel()
        }
      }
        .isInstanceOf(CallTimedOut::class)
      val start = kotlin.time.TimeSource.Monotonic.markNow()
      assertFailure {
        flight.await("key", scope, 1.minutes) {
          runs.incrementAndFetch()
          5
        }
      }.isInstanceOf(CallStuck::class)

      assertThat(start.elapsedNow() < 1.seconds).isTrue()
      assertThat(runs.load()).isEqualTo(1)
      assertThat(flight.await("other", scope, 1.minutes) { 6 }).isEqualTo(6)
    }

  @Test
  fun `a call that ends on the cancel frees its key at its timeout`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()

    assertFailure {
      flight.await("key", scope, 50.milliseconds) { awaitCancellation() }
    }.isInstanceOf(CallTimedOut::class)
    assertThat(flight.await("key", scope, 1.minutes) { 5 }).isEqualTo(5)
  }

  @Test
  fun `a stuck call that finally resumes frees its key`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    val parked = CompletableDeferred<kotlin.coroutines.Continuation<Int>>()

    assertFailure {
      flight.await("key", scope, 50.milliseconds) { kotlin.coroutines.suspendCoroutine { parked.complete(it) } }
    }.isInstanceOf(CallTimedOut::class)
    parked.await().resumeWith(Result.success(1))
    kotlinx.coroutines.delay(50)

    assertThat(flight.await("key", scope, 1.minutes) { 7 }).isEqualTo(7)
  }

  @Test
  fun `a waiter that joined a stuck call times out too`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    val first = scope.async { runCatching { flight.await("key", scope, 1.minutes) { neverResumesIgnoringCancel() } } }
    kotlinx.coroutines.delay(50)

    assertFailure { flight.await("key", scope, 50.milliseconds) { 1 } }.isInstanceOf(CallTimedOut::class)
    first.cancelAndJoin()
  }

  @Test
  fun `a call in a scope that is already cancelled fails without blocking the next one`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    val cancelled = CoroutineScope(Job()).apply { cancel() }

    assertFailure { flight.await("key", cancelled, 1.minutes) { 1 } }
    assertThat(flight.await("key", scope, 1.minutes) { 2 }).isEqualTo(2)
  }

  @Test
  fun `the call is cancelled when its last waiter leaves and not before`() = runBlocking<Unit> {
    val flight = SingleFlight<String, Int>()
    val cancelled = CompletableDeferred<Unit>()
    val block: suspend () -> Int = {
      try {
        awaitCancellation()
      } finally {
        cancelled.complete(Unit)
      }
    }
    val first = scope.async { flight.await("key", scope, 1.minutes, block) }
    kotlinx.coroutines.delay(20)
    val second = scope.async { flight.await("key", scope, 1.minutes, block) }
    kotlinx.coroutines.delay(20)

    first.cancel()
    kotlinx.coroutines.delay(50)
    assertThat(cancelled.isCompleted).isFalse()
    second.cancel()
    assertThat(withTimeout(5_000) { cancelled.await() }).isEqualTo(Unit)
  }
}
