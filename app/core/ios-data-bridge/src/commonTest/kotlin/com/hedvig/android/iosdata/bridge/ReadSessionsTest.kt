@file:OptIn(ExperimentalAtomicApi::class)

package com.hedvig.android.iosdata.bridge

import assertk.assertFailure
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isSameInstanceAs
import assertk.assertions.isTrue
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.suspendCoroutine
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeSource
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/** The read contract on its own, on every platform: no Swift, no `NativeSuspend`. */
class ReadSessionsTest {
  private val logs = CapturingLogger()
  private val memberClient = ApolloClient.Builder().serverUrl("https://member.test").build()
  private val publicClient = ApolloClient.Builder().serverUrl("https://public.test").build()
  private val reads = ReadSessions(MemberReadScope(memberClient), PublicReadScope(publicClient))
  private val callers = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  @BeforeTest
  fun install() {
    LogcatLogger.install(logs)
  }

  @AfterTest
  fun uninstall() {
    LogcatLogger.uninstall()
    memberClient.close()
    publicClient.close()
  }

  private fun <T> start(block: suspend () -> T): Deferred<T> = callers.async { block() }

  private fun <T> member(name: String, vararg arguments: Any?, block: suspend MemberReadScope.() -> T): T =
    runBlocking { withTimeout(WAIT) { reads.read(name, arguments.toList(), "TestOperation", block) } }

  private fun <T> public(name: String, vararg arguments: Any?, block: suspend PublicReadScope.() -> T): T =
    runBlocking { withTimeout(WAIT) { reads.publicRead(name, arguments.toList(), "PublicOperation", block) } }

  private fun failureOf(block: () -> Any?): ReadFailure {
    try {
      block()
    } catch (e: ReadFailure) {
      return e
    }
    error("The read did not fail")
  }

  @Test
  fun `a member read gets the member client and a logged-out read the public one`() {
    assertThat(member("test.clients") { apolloClient }).isSameInstanceAs(memberClient)
    assertThat(public("test.clients") { apolloClient }).isSameInstanceAs(publicClient)
  }

  @Test
  fun `a read failure arrives as a general failure whose message is its kind`() {
    val failure = failureOf { member("test.failure") { throw generalReadFailure() } }

    assertThat(failure.kind).isEqualTo(ReadFailureKind.GENERAL)
    assertThat(failure.message).isEqualTo("GENERAL")
  }

  @Test
  fun `any other exception arrives as a general failure without its text`() {
    val failure = failureOf { member("test.bug") { throw IllegalStateException("member-sentinel-91c2") } }

    assertThat(failure.kind).isEqualTo(ReadFailureKind.GENERAL)
    assertThat(failure.message).isEqualTo("GENERAL")
    assertThat(logs.messages().none { "member-sentinel-91c2" in it }).isTrue()
  }

  @Test
  fun `a failure is logged with the read name and the kind only`() {
    failureOf { member("test.logged") { throw IllegalStateException("member-sentinel-3e1f") } }
    failureOf { member("test.kind") { throw generalReadFailure() } }

    assertThat(logs.entries()).containsExactly(
      LogPriority.ERROR to "Read test.logged failed unexpectedly: IllegalStateException",
      LogPriority.WARN to "Read test.kind failed: GENERAL UNEXPECTED",
    )
  }

  @Test
  fun `a failure carries the read and the operation and the reason for the iOS app to log`() {
    reads.readTimeout = 200.milliseconds

    val failed = failureOf { member("test.telemetry") { throw generalReadFailure() } }
    val timedOut = failureOf { member("test.slow") { awaitCancellation() } }

    assertThat(listOf(failed.kind, failed.reason, failed.readName, failed.operationName))
      .containsExactly(ReadFailureKind.GENERAL, ReadFailureReason.UNEXPECTED, "test.telemetry", "TestOperation")
    assertThat(timedOut.reason).isEqualTo(ReadFailureReason.TIMEOUT)
  }

  @Test
  fun `a cancellation raised inside a read nobody cancelled is a general failure`() {
    val failure = failureOf { member("test.inner") { throw CancellationException("inner") } }

    assertThat(failure.kind).isEqualTo(ReadFailureKind.GENERAL)
  }

  @Test
  fun `the caller's own cancel stays a cancel and is no failure`() = runBlocking {
    val started = CompletableDeferred<Unit>()
    val caller = start {
      reads.read("test.cancel", emptyList(), "TestOperation") {
        started.complete(Unit)
        awaitCancellation()
      }
    }
    started.await()

    caller.cancel()

    assertFailure { caller.await() }.isInstanceOf(CancellationException::class)
    assertThat(logs.messages().none { "test.cancel" in it }).isTrue()
  }

  @Test
  fun `a read stuck in an iOS app input makes the same read fail at once until it ends but no other read`() {
    reads.readTimeout = 200.milliseconds
    val calls = AtomicInt(0)

    val stuck = failureOf {
      member("test.stuck") {
        calls.incrementAndFetch()
        suspendCoroutine<Int> { }
      }
    }
    val mark = TimeSource.Monotonic.markNow()
    val again = failureOf {
      member("test.stuck") {
        calls.incrementAndFetch()
        1
      }
    }
    val againTook = mark.elapsedNow()
    val other = member("test.stuck", "other-argument") { 2 }

    assertThat(stuck.reason).isEqualTo(ReadFailureReason.TIMEOUT)
    assertThat(again.reason).isEqualTo(ReadFailureReason.STUCK)
    assertThat(againTook < 150.milliseconds).isTrue()
    assertThat(calls.load()).isEqualTo(1)
    assertThat(other).isEqualTo(2)
  }

  @Test
  fun `a read refused because it is stuck logs that it is stuck and not that it timed out`() {
    reads.readTimeout = 200.milliseconds

    failureOf { member("test.stucklog") { suspendCoroutine<Int> { } } }
    failureOf { member("test.stucklog") { 1 } }

    assertThat(logs.entries()).containsExactly(
      LogPriority.WARN to "Read test.stucklog timed out",
      LogPriority.WARN to "Read test.stucklog refused: the same read is stuck waiting on an iOS app input",
    )
  }

  @Test
  fun `a session change ends a member read in flight as a session ended failure`() = runBlocking {
    val started = CompletableDeferred<Unit>()
    val caller = start {
      runCatching {
        reads.read("test.session", emptyList(), "TestOperation") {
          started.complete(Unit)
          awaitCancellation()
        }
      }
    }
    started.await()

    reads.sessionChanged()

    val failure = withTimeout(WAIT) { caller.await() }.exceptionOrNull() as ReadFailure
    assertThat(failure.kind).isEqualTo(ReadFailureKind.SESSION_ENDED)
  }

  @Test
  fun `a session change ends a read stuck in an iOS app input at once and its key is not stuck`() = runBlocking {
    reads.readTimeout = 60.seconds
    val started = CompletableDeferred<Unit>()
    val caller = start {
      runCatching {
        reads.read("test.stuckend", emptyList(), "TestOperation") {
          started.complete(Unit)
          suspendCoroutine<Int> { }
        }
      }
    }
    started.await()
    val mark = TimeSource.Monotonic.markNow()

    reads.sessionChanged()

    val failure = withTimeout(WAIT) { caller.await() }.exceptionOrNull() as ReadFailure
    assertThat(mark.elapsedNow() < 2.seconds).isTrue()
    assertThat(failure.kind).isEqualTo(ReadFailureKind.SESSION_ENDED)
    assertThat(reads.stuckReads()).isEqualTo(0)
    assertThat(member("test.stuckend") { 1 }).isEqualTo(1)
  }

  @Test
  fun `a session change forgets the stuck reads of the old session`() {
    reads.readTimeout = 200.milliseconds
    failureOf { member("test.forget") { suspendCoroutine<Int> { } } }
    assertThat(reads.stuckReads()).isEqualTo(1)

    reads.sessionChanged()

    assertThat(reads.stuckReads()).isEqualTo(0)
  }

  @Test
  fun `a read started right after a session change runs in the new session`() = runBlocking {
    val oldSession = CompletableDeferred<Unit>()
    start {
      runCatching {
        reads.read("test.joined", emptyList(), "TestOperation") {
          oldSession.complete(Unit)
          suspendCoroutine<Int> { }
        }
      }
    }
    oldSession.await()

    reads.sessionChanged()

    assertThat(member("test.joined") { 3 }).isEqualTo(3)
  }

  @Test
  fun `equal reads in flight are joined`() = runBlocking {
    val gate = CompletableDeferred<Int>()
    val runs = AtomicInt(0)
    val read: suspend () -> Int = {
      reads.read("test.join", emptyList(), "TestOperation") {
        runs.incrementAndFetch()
        gate.await()
      }
    }
    val first = start(read)
    val second = start(read)
    waitUntil { reads.waitingCallers() == 2 }
    gate.complete(5)

    assertThat(listOf(first.await(), second.await())).containsExactly(5, 5)
    assertThat(runs.load()).isEqualTo(1)
  }

  @Test
  fun `reads with several arguments are joined only when every argument is equal`() = runBlocking {
    val gate = CompletableDeferred<Unit>()
    val runs = AtomicInt(0)

    fun read(a: String, b: Int) = start {
      reads.read("test.args", listOf(a, b), "TestOperation") {
        runs.incrementAndFetch()
        gate.await()
        "$a-$b"
      }
    }
    val first = read("x", 1)
    val sameAgain = read("x", 1)
    val otherSecond = read("x", 2)
    // All three callers wait before the gate opens, so the second "x-1" can only join the first.
    waitUntil { reads.waitingCallers() == 3 }
    gate.complete(Unit)

    assertThat(listOf(first.await(), sameAgain.await(), otherSecond.await())).containsExactly("x-1", "x-1", "x-2")
    assertThat(runs.load()).isEqualTo(2)
  }

  @Test
  fun `a logged-out read is not ended by a session change`() = runBlocking {
    val started = CompletableDeferred<Unit>()
    val gate = CompletableDeferred<Int>()
    val caller = start {
      reads.publicRead("test.public", emptyList(), "PublicOperation") {
        started.complete(Unit)
        gate.await()
      }
    }
    started.await()

    reads.sessionChanged()
    gate.complete(9)

    assertThat(withTimeout(WAIT) { caller.await() }).isEqualTo(9)
  }

  @Test
  fun `a logged-out read stuck in an iOS app input is bounded and stays refused across a session change`() {
    reads.readTimeout = 200.milliseconds

    val stuck = failureOf { public("test.publicstuck") { suspendCoroutine<Int> { } } }
    reads.sessionChanged()
    val again = failureOf { public("test.publicstuck") { 1 } }
    val memberRead = member("test.publicstuck") { 2 }

    assertThat(stuck.reason).isEqualTo(ReadFailureReason.TIMEOUT)
    assertThat(again.reason).isEqualTo(ReadFailureReason.STUCK)
    assertThat(memberRead).isEqualTo(2)
  }

  @Test
  fun `a logged-out read fails like a member read with its name and operation`() {
    val failed = failureOf { public("test.publicfail") { throw generalReadFailure() } }

    assertThat(listOf(failed.kind, failed.readName, failed.operationName))
      .containsExactly(ReadFailureKind.GENERAL, "test.publicfail", "PublicOperation")
  }

  private companion object {
    val WAIT: Duration = 5.seconds
  }
}

internal fun waitUntil(timeout: Duration = 5.seconds, condition: () -> Boolean) {
  val mark = TimeSource.Monotonic.markNow()
  runBlocking {
    while (!condition()) {
      check(mark.elapsedNow() < timeout) { "Condition not met within $timeout" }
      delay(5.milliseconds)
    }
  }
}

internal class CapturingLogger : LogcatLogger {
  private val lock = reentrantLock()
  private val captured = mutableListOf<Pair<LogPriority, String>>()

  fun entries(): List<Pair<LogPriority, String>> = lock.withLock { captured.toList() }

  fun messages(): List<String> = entries().map { it.second }

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val text = message()
    lock.withLock { captured += priority to text }
  }
}
