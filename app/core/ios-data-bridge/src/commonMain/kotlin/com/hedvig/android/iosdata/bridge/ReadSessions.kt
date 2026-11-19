@file:OptIn(ExperimentalAtomicApi::class, ExperimentalObjCRefinement::class)

package com.hedvig.android.iosdata.bridge

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.selects.select

/** What a member read may use: the client that sends the member's token. */
@HiddenFromObjC
class MemberReadScope internal constructor(val apolloClient: ApolloClient)

/** What a logged-out read may use: the client that never sends a token and never asks for one. */
@HiddenFromObjC
class PublicReadScope internal constructor(val apolloClient: ApolloClient)

/**
 * The suspend core of every read the iOS app makes, the same for every feature:
 * - Every failure is a [ReadFailure]; the caller's own cancel stays a cancel.
 * - A caller waits at most [readTimeout], also when an iOS app input (the token or device id fetch) never answers,
 *   then fails as `TIMEOUT`. While a read is stuck in such an input, which Kotlin's cancel cannot reach, the same read
 *   (name, arguments and session) fails at once as `STUCK` instead of starting another, so at most one coroutine per
 *   read and session is stuck.
 * - Equal reads in flight are joined.
 * - Failures are logged with the read's name and the failure kind and reason only.
 *
 * A member read ([read]) gets only the member client and belongs to the session it started in: [sessionChanged] ends
 * it as `SESSION_ENDED`. A logged-out read ([publicRead]) gets only the token-free client, belongs to no session and is
 * never ended by one.
 */
internal class ReadSessions(
  private val member: MemberReadScope,
  private val public: PublicReadScope,
) {
  private class Session(val number: Long, val job: CompletableJob) {
    val scope: CoroutineScope = CoroutineScope(job + Dispatchers.Default)
  }

  private data class ReadKey(val name: String, val arguments: List<Any?>, val session: Long)

  private val current = AtomicReference(Session(0, SupervisorJob()))
  private val flights = SingleFlight<ReadKey, Any?>(
    stillCurrent = { it.session == NO_SESSION || it.session == current.load().number },
  )

  /** Where logged-out reads run; nothing cancels it. */
  private val publicScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  /** How long a caller waits for a read. */
  var readTimeout: Duration = 60.seconds

  /** The member read [block] in the session current at this call. See [SessionReads.read] for the parameters. */
  suspend fun <T> read(
    name: String,
    arguments: List<Any?>,
    operation: String,
    block: suspend MemberReadScope.() -> T,
  ): T {
    val session = current.load()
    return failingAsReadFailure(name, operation, session.job) {
      untilSessionEnds(session.job) {
        @Suppress("UNCHECKED_CAST")
        flights.await(ReadKey(name, arguments, session.number), session.scope, readTimeout) { member.block() } as T
      }
    }
  }

  /** The logged-out read [block]. See [SessionReads.publicRead] for the parameters. */
  suspend fun <T> publicRead(
    name: String,
    arguments: List<Any?>,
    operation: String,
    block: suspend PublicReadScope.() -> T,
  ): T = failingAsReadFailure(name, operation, session = null) {
    @Suppress("UNCHECKED_CAST")
    flights.await(ReadKey(name, arguments, NO_SESSION), publicScope, readTimeout) { public.block() } as T
  }

  /** How many reads are stuck in an iOS app input now. */
  fun stuckReads(): Int = flights.stuckCount()

  /** How many callers wait on reads in flight now. */
  fun waitingCallers(): Int = flights.waiterCount()

  /** Ends the member reads in flight, synchronously, so no read started after this call runs in the previous session. */
  fun sessionChanged() {
    while (true) {
      val old = current.load()
      val new = Session(old.number + 1, SupervisorJob())
      if (current.compareAndSet(old, new)) {
        old.job.cancel()
        flights.forgetStuck { it.session != NO_SESSION && it.session != new.number }
        return
      }
    }
  }
}

/** The session number of a logged-out read. Member sessions count up from 0. */
private const val NO_SESSION = -1L

/**
 * Runs [block] until it ends or [session] is cancelled, whichever is first; a session that ends first is
 * `SESSION_ENDED` at once, also when [block] waits on a call stuck in an iOS app input, which never completes.
 */
private suspend fun <T> untilSessionEnds(session: Job, block: suspend () -> T): T = coroutineScope {
  // A child of the session, without children of its own, ends the moment the session is cancelled.
  val sessionWatch = Job(session)
  try {
    val work = async { block() }
    select {
      work.onAwait { it }
      sessionWatch.onJoin {
        work.cancel()
        throw failure(ReadFailureKind.SESSION_ENDED, ReadFailureReason.SESSION_ENDED)
      }
    }
  } finally {
    sessionWatch.complete()
  }
}

/**
 * Runs [block], turning anything it throws into a [ReadFailure]. Only the caller's cancel ends it as cancelled; the end
 * of [session] is `SESSION_ENDED`, and any other cancellation raised inside the read is a general failure.
 */
private suspend fun <T> failingAsReadFailure(
  name: String,
  operation: String,
  session: Job?,
  block: suspend () -> T,
): T = try {
  block()
} catch (e: CancellationException) {
  currentCoroutineContext().ensureActive()
  if (session != null && session.isCancelled) {
    logcat(LogPriority.INFO) { "Read $name ended with its session" }
    throw failure(ReadFailureKind.SESSION_ENDED, ReadFailureReason.SESSION_ENDED).within(name, operation)
  }
  logcat(LogPriority.ERROR) { "Read $name raised a cancellation nobody asked for" }
  throw failure(ReadFailureKind.GENERAL, ReadFailureReason.UNEXPECTED).within(name, operation)
} catch (e: ReadFailure) {
  currentCoroutineContext().ensureActive()
  logcat(LogPriority.WARN) { "Read $name failed: ${e.kind} ${e.reason}" }
  throw e.within(name, operation)
} catch (e: CallStuck) {
  currentCoroutineContext().ensureActive()
  logcat(LogPriority.WARN) { "Read $name refused: the same read is stuck waiting on an iOS app input" }
  throw failure(ReadFailureKind.GENERAL, ReadFailureReason.STUCK).within(name, operation)
} catch (e: CallTimedOut) {
  currentCoroutineContext().ensureActive()
  logcat(LogPriority.WARN) { "Read $name timed out" }
  throw failure(ReadFailureKind.GENERAL, ReadFailureReason.TIMEOUT).within(name, operation)
} catch (e: Throwable) {
  currentCoroutineContext().ensureActive()
  logcat(LogPriority.ERROR) { "Read $name failed unexpectedly: ${e::class.simpleName}" }
  throw failure(ReadFailureKind.GENERAL, ReadFailureReason.UNEXPECTED).within(name, operation)
}
