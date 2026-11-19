package com.hedvig.android.iosdata.bridge

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.logger.LogcatLogger
import com.rickclephas.kmp.nativecoroutines.NativeCancellable
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import platform.Foundation.NSError

/** What only the Objective-C adapter adds to the read contract: the callbacks the iOS app receives. */
class SessionReadsTest {
  private val memberClient = ApolloClient.Builder().serverUrl("https://member.test").build()
  private val publicClient = ApolloClient.Builder().serverUrl("https://public.test").build()
  private val reads = SessionReads(memberClient, publicClient)

  @BeforeTest
  fun install() {
    LogcatLogger.install(CapturingLogger())
  }

  @AfterTest
  fun uninstall() {
    LogcatLogger.uninstall()
    memberClient.close()
    publicClient.close()
  }

  @Test
  fun `a read delivers its value once`() {
    val started = reads.read("test.value", operation = "TestOperation") { 7 }.begin()

    waitUntil { started.result.value != null }
    runBlocking { delay(50.milliseconds) }

    assertThat(started.result.value?.value).isEqualTo(7)
    assertThat(started.callbacks.value).isEqualTo(1)
  }

  @Test
  fun `a failure arrives as an error that ReadFailures reads`() {
    val delivered = reads.read("test.failure", operation = "TestOperation") { throw generalReadFailure() }.within(WAIT)

    assertThat(delivered.failure?.kind).isEqualTo(ReadFailureKind.GENERAL)
    assertThat(delivered.failure?.readName).isEqualTo("test.failure")
  }

  @Test
  fun `a cancel from the caller is delivered once and reads as no failure`() {
    val started = CompletableDeferred<Unit>()
    val delivered = reads.read("test.cancel", operation = "TestOperation") {
      started.complete(Unit)
      awaitCancellation()
    }.begin()
    runBlocking { started.await() }

    delivered.cancel()

    waitUntil { delivered.callbacks.value == 1 }
    assertThat(delivered.result.value?.cancellation).isNotNull()
    assertThat(ReadFailures.from(delivered.result.value!!.cancellation!!)).isNull()
    runBlocking { delay(100.milliseconds) }
    assertThat(delivered.callbacks.value).isEqualTo(1)
  }

  @Test
  fun `a session change ends a member read as an error and not as a cancel`() {
    val started = CompletableDeferred<Unit>()
    val delivered = reads.read("test.session", operation = "TestOperation") {
      started.complete(Unit)
      awaitCancellation()
    }.begin()
    runBlocking { started.await() }

    reads.sessionChanged()

    waitUntil { delivered.result.value != null }
    assertThat(delivered.result.value!!.cancellation).isNull()
    assertThat(delivered.result.value!!.failure?.kind).isEqualTo(ReadFailureKind.SESSION_ENDED)
  }

  @Test
  fun `a read built before a session change and awaited after it runs in the new session`() {
    val read = reads.read("test.built", operation = "TestOperation") { 4 }

    reads.sessionChanged()

    assertThat(read.within(WAIT).value).isEqualTo(4)
  }

  @Test
  fun `a logged-out read delivers through the same adapter`() {
    assertThat(reads.publicRead("test.public", operation = "PublicOperation") { 6 }.within(WAIT).value).isEqualTo(6)
  }

  private companion object {
    val WAIT: Duration = 5.seconds
  }
}

internal class Delivered<T>(val value: T?, val error: NSError?, val cancellation: NSError?) {
  val failure: ReadFailure? get() = error?.let { ReadFailures.from(it) }
}

internal class Started<T> {
  val result = AtomicReference<Delivered<T>?>(null)
  val callbacks = AtomicInt(0)
  internal var cancelCall: NativeCancellable = {}

  fun cancel() {
    cancelCall()
  }

  fun record(delivered: Delivered<T>) {
    callbacks.incrementAndGet()
    result.compareAndSet(null, delivered)
  }
}

/** Starts the read as `asyncFunction(for:)` does and records every callback. */
internal fun <T> NativeSuspend<T>.begin(): Started<T> {
  val started = Started<T>()
  started.cancelCall = this(
    { value, unit -> started.record(Delivered(value, null, null)).let { unit } },
    { error, unit -> started.record(Delivered(null, error, null)).let { unit } },
    { error, unit -> started.record(Delivered(null, null, error)).let { unit } },
  )
  return started
}

internal fun <T> NativeSuspend<T>.within(timeout: Duration): Delivered<T> {
  val started = begin()
  waitUntil(timeout) { started.result.value != null }
  return started.result.value!!
}
