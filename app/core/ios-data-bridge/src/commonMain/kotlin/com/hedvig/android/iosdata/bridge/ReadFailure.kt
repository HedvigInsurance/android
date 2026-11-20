@file:OptIn(ExperimentalObjCEnum::class, ExperimentalObjCRefinement::class)

package com.hedvig.android.iosdata.bridge

import arrow.core.getOrElse
import arrow.core.raise.Raise
import arrow.core.raise.either
import com.apollographql.apollo.exception.ApolloNetworkException
import com.hedvig.android.apollo.ApolloOperationError
import com.hedvig.android.apollo.WrapsApolloOperationError
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import kotlin.coroutines.cancellation.CancellationException
import kotlin.experimental.ExperimentalObjCEnum
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCEnum

/** Why a read the iOS app awaits failed. Swift finds it in the `KotlinException` entry of the error it catches. */
class ReadFailure(val kind: ReadFailureKind, cause: Throwable? = null) : Exception(kind.name, cause)

@ObjCEnum
enum class ReadFailureKind {
  /** No answer arrived. */
  NETWORK,

  /** The backend rejected the member's token. */
  UNAUTHENTICATED,

  /** Anything else. */
  OTHER,
}

/**
 * Runs a read for the iOS app. A failure [block] raises (an [ApolloOperationError], an [ErrorMessage], or anything
 * else) or throws is thrown as a [ReadFailure]; a cancellation passes through unchanged. An [ErrorMessage] made from an
 * [ApolloOperationError] fails the way that error would.
 */
@HiddenFromObjC
suspend fun <T> iosRead(block: suspend Raise<Any>.() -> T): T = try {
  either { block() }.getOrElse { throw readFailureOf(it) }
} catch (e: CancellationException) {
  throw e
} catch (e: ReadFailure) {
  throw e
} catch (e: Throwable) {
  throw ReadFailure(kindOf(e), e)
}

private fun readFailureOf(error: Any): ReadFailure = when (error) {
  is ApolloOperationError -> {
    ReadFailure(
      if (error.containsUnauthenticatedError) ReadFailureKind.UNAUTHENTICATED else kindOf(error.throwable),
      error.throwable,
    )
  }

  is WrapsApolloOperationError -> {
    readFailureOf(error.apolloOperationError)
  }

  is ErrorMessage -> {
    ReadFailure(kindOf(error.throwable), error.throwable)
  }

  else -> {
    logcat(LogPriority.WARN) { "An iOS read raised an unexpected ${error::class.simpleName}" }
    ReadFailure(ReadFailureKind.OTHER)
  }
}

private fun kindOf(throwable: Throwable?): ReadFailureKind = when (throwable) {
  is ApolloNetworkException -> ReadFailureKind.NETWORK
  else -> ReadFailureKind.OTHER
}
