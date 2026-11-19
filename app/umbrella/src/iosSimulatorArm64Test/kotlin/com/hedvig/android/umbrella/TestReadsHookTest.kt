package com.hedvig.android.umbrella

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.iosdata.bridge.ReadFailureKind
import com.hedvig.android.iosdata.bridge.ReadFailureReason
import com.hedvig.android.iosdata.bridge.ReadFailures
import com.hedvig.android.logger.IosLogcatLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import kotlin.concurrent.AtomicReference
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import platform.Foundation.NSError

/**
 * The hook the iOS app's tests use, driven the way they drive it: canned answers, the real graph, the real reads, the
 * example reads reached through the holder as a feature's are.
 */
class TestReadsHookTest {
  private val sink = RecordingSink()

  @BeforeTest
  fun installSink() {
    IosLogcatLogger.install(sink, LogPriority.VERBOSE)
  }

  @AfterTest
  fun uninstallLoggers() {
    LogcatLogger.uninstall()
    RumLogger.uninstall()
  }

  @Test
  fun `a canned answer reaches the member read with the member's token`() {
    val backend = CannedBackend().apply { answer(OPERATION, CONVERSATIONS_JSON) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)

    val read = test.holder.exampleReads().memberConversations().readWithin()

    assertThat(read.value?.openCount).isEqualTo(1)
    assertThat(backend.sentAuthorizations()).containsExactly("Bearer test-access-token")
  }

  @Test
  fun `a failed operation gives a general failure with the names the iOS app logs`() {
    val test = TestGraphs.create(CannedBackend(), tokenFetchNeverAnswers = false)

    val failure = test.holder.exampleReads().memberConversations().readWithin().failure!!

    assertThat(listOf(failure.kind, failure.reason, failure.readName, failure.operationName))
      .containsExactly(ReadFailureKind.GENERAL, ReadFailureReason.HTTP, "example.memberConversations", OPERATION)
    assertThat(sink.messages().filter { it.startsWith("Read ") })
      .containsExactly("Read example.memberConversations failed: GENERAL HTTP")
  }

  @Test
  fun `a held operation and the end of a session give a session ended failure`() {
    val backend = CannedBackend().apply { hold(OPERATION) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)
    val delivered = test.holder.exampleReads().memberConversations().startRead()
    check(pumpMainLoopUntil { backend.sentOperations().isNotEmpty() })

    test.holder.sessionEnded()

    assertThat(pumpMainLoopUntil { delivered.value != null }).isTrue()
    assertThat(delivered.value!!.cancellation).isNull()
    assertThat(delivered.value!!.failure?.kind).isEqualTo(ReadFailureKind.SESSION_ENDED)
    backend.release(OPERATION)
  }

  @Test
  fun `the start of a session ends a member read in flight too`() {
    val backend = CannedBackend().apply { hold(OPERATION) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)
    val delivered = test.holder.exampleReads().memberConversations().startRead()
    check(pumpMainLoopUntil { backend.sentOperations().isNotEmpty() })

    test.holder.sessionStarted()

    assertThat(pumpMainLoopUntil { delivered.value != null }).isTrue()
    assertThat(delivered.value!!.failure?.kind).isEqualTo(ReadFailureKind.SESSION_ENDED)
    backend.release(OPERATION)
  }

  @Test
  fun `a member read waiting on a token fetch that never answers fails within its timeout`() {
    val backend = CannedBackend().apply { answer(OPERATION, CONVERSATIONS_JSON) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = true)
    test.graph.sessionReads.readTimeout = 300.milliseconds

    val first = test.holder.exampleReads().memberConversations().readWithin()
    val second = test.holder.exampleReads().memberConversations().readWithin()

    assertThat(first.failure?.reason).isEqualTo(ReadFailureReason.TIMEOUT)
    assertThat(second.failure?.reason).isEqualTo(ReadFailureReason.STUCK)
    assertThat(test.tokenFetchCount()).isEqualTo(1)
    assertThat(backend.sentOperations()).containsExactly()
  }

  @Test
  fun `a logged-out read sends no token and asks for none while the token fetch hangs`() {
    val backend = CannedBackend().apply { answer(OPERATION, CONVERSATIONS_JSON) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = true)
    test.graph.sessionReads.readTimeout = 300.milliseconds

    val read = test.holder.exampleReads().publicConversations().readWithin()

    assertThat(read.value?.openCount).isEqualTo(1)
    assertThat(test.tokenFetchCount()).isEqualTo(0)
    assertThat(backend.sentAuthorizations()).containsExactly("")
  }

  @Test
  fun `a logged-out read sends no token while a member is logged in`() {
    val backend = CannedBackend().apply { answer(OPERATION, CONVERSATIONS_JSON) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)
    test.holder.sessionStarted()

    test.holder.exampleReads().memberConversations().readWithin()
    test.holder.exampleReads().publicConversations().readWithin()

    assertThat(backend.sentAuthorizations()).containsExactly("Bearer test-access-token", "")
  }

  @Test
  fun `a login or a logout does not end a logged-out read in flight`() {
    val backend = CannedBackend().apply {
      answer(OPERATION, CONVERSATIONS_JSON)
      hold(OPERATION)
    }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)
    val delivered = test.holder.exampleReads().publicConversations().startRead()
    check(pumpMainLoopUntil { backend.sentOperations().isNotEmpty() })

    test.holder.sessionStarted()
    test.holder.sessionEnded()
    backend.release(OPERATION)

    assertThat(pumpMainLoopUntil { delivered.value != null }).isTrue()
    assertThat(delivered.value!!.value?.openCount).isEqualTo(1)
  }

  @Test
  fun `a cancel by the awaiting task reads as no failure`() {
    val backend = CannedBackend().apply { hold(OPERATION) }
    val test = TestGraphs.create(backend, tokenFetchNeverAnswers = false)
    var cancellation: NSError? = null
    val cancel = test.holder.exampleReads().memberConversations()(
      { _, unit -> unit },
      { _, unit -> unit },
      { error, unit ->
        cancellation = error
        unit
      },
    )

    cancel()

    assertThat(cancellation).isNotNull()
    assertThat(ReadFailures.from(cancellation!!)).isNull()
    backend.release(OPERATION)
  }
}

/** What a read delivered to the iOS app: one of a value, an error, or a cancellation. */
internal class ReadCall<T>(val value: T?, val error: NSError?, val cancellation: NSError?) {
  val failure get() = error?.let { ReadFailures.from(it) }
}

/** Starts the read as `asyncFunction(for:)` does; the reference is set by the first callback. */
internal fun <T> NativeSuspend<T>.startRead(): AtomicReference<ReadCall<T>?> {
  val delivered = AtomicReference<ReadCall<T>?>(null)
  this(
    { value, unit -> delivered.compareAndSet(null, ReadCall(value, null, null)).let { unit } },
    { error, unit -> delivered.compareAndSet(null, ReadCall(null, error, null)).let { unit } },
    { error, unit -> delivered.compareAndSet(null, ReadCall(null, null, error)).let { unit } },
  )
  return delivered
}

private fun <T> NativeSuspend<T>.readWithin(): ReadCall<T> {
  val delivered = startRead()
  check(pumpMainLoopUntil(10.seconds) { delivered.value != null }) { "The read did not call back" }
  return delivered.value!!
}

private const val OPERATION = "CbmNumberOfChatMessages"
private const val CONVERSATIONS_JSON = """
{"data": {"currentMember": {"__typename": "Member", "id": "member-1", "legacyConversation": null, "conversations": [
  {"__typename": "Conversation", "id": "conversation-1", "isOpen": true, "newestMessage": null},
  {"__typename": "Conversation", "id": "conversation-2", "isOpen": false, "newestMessage": null}]}}}
"""
