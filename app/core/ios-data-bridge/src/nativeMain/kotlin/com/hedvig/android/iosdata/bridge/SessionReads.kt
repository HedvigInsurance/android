@file:OptIn(ExperimentalObjCRefinement::class)

package com.hedvig.android.iosdata.bridge

import com.apollographql.apollo.ApolloClient
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.common.di.PublicApolloClient
import com.rickclephas.kmp.nativecoroutines.NativeSuspend
import com.rickclephas.kmp.nativecoroutines.nativeSuspend
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.time.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/**
 * Turns a suspend read into the `NativeSuspend` the iOS app awaits with `asyncFunction(for:)`. Every read of every
 * feature goes through here, with the contract of [ReadSessions]: exactly one callback, a cancel by the awaiting task
 * delivered at once with nothing after it, every failure a [ReadFailure] that [ReadFailures.from] reads, a bounded wait
 * even when an iOS app input never answers.
 *
 * The block of a read starts when the iOS app starts awaiting, so a read built before a session change and awaited after
 * it runs in the new session. A member read's block receives only the member client and a logged-out read's block only
 * the token-free one, so the wrong client cannot be reached through either.
 */
@HiddenFromObjC
@SingleIn(AppScope::class)
@Inject
class SessionReads(
  memberClient: ApolloClient,
  @PublicApolloClient publicClient: ApolloClient,
) {
  internal val core = ReadSessions(MemberReadScope(memberClient), PublicReadScope(publicClient))

  /** Where callers wait; only a caller's own cancel cancels a waiter. */
  private val waiters = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  /** How long a caller waits for a read. */
  var readTimeout: Duration
    get() = core.readTimeout
    set(value) {
      core.readTimeout = value
    }

  /**
   * The member read [block] under [name], a stable dotted name that logs show (`forever.referrals`), sending
   * [operation], the GraphQL operation the iOS app may log on failure, and [arguments], every value that makes two
   * reads of the same name different (a contract id, a page cursor); reads are joined only when all of them are equal.
   * A login or logout while it runs ends it as `SESSION_ENDED`.
   */
  fun <T> read(
    name: String,
    vararg arguments: Any?,
    operation: String,
    block: suspend MemberReadScope.() -> T,
  ): NativeSuspend<T> {
    val key = arguments.toList()
    return nativeSuspend(waiters) { core.read(name, key, operation, block) }.deliveringOnce()
  }

  /** A read made before login, as [read] in everything but the client and the session, which never ends it. */
  fun <T> publicRead(
    name: String,
    vararg arguments: Any?,
    operation: String,
    block: suspend PublicReadScope.() -> T,
  ): NativeSuspend<T> {
    val key = arguments.toList()
    return nativeSuspend(waiters) { core.publicRead(name, key, operation, block) }.deliveringOnce()
  }

  /** Ends the member reads in flight, synchronously, so no read started after this call runs in the previous session. */
  fun sessionChanged() {
    core.sessionChanged()
  }
}
