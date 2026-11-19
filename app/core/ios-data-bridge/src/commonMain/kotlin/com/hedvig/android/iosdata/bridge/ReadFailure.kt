@file:OptIn(ExperimentalObjCRefinement::class, ExperimentalObjCEnum::class)

package com.hedvig.android.iosdata.bridge

import com.apollographql.apollo.exception.ApolloHttpException
import com.apollographql.apollo.exception.ApolloNetworkException
import com.hedvig.android.core.common.ErrorMessage
import kotlin.experimental.ExperimentalObjCEnum
import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC
import kotlin.native.ObjCEnum

/**
 * Why a read made through [SessionReads] failed. Every failure of such a read arrives as one of these, a Kotlin bug
 * included. It carries no member data and no exception text, so the iOS app may log every field.
 */
class ReadFailure internal constructor(
  val kind: ReadFailureKind,
  val reason: ReadFailureReason,
  /** The read's name, such as `insurances.contracts`. */
  val readName: String,
  /** The GraphQL operation the read sends, such as `InsuranceContracts`. */
  val operationName: String,
) : Exception(kind.name) {
  internal fun within(readName: String, operationName: String) = ReadFailure(kind, reason, readName, operationName)

  override fun toString(): String = "ReadFailure($kind, $reason, $readName, $operationName)"
}

/** What the caller can do about a [ReadFailure]. */
@ObjCEnum
enum class ReadFailureKind {
  /** The read failed and may be retried. */
  GENERAL,

  /**
   * A login or logout ended the session the read ran in while the caller was still waiting. The caller's own cancel
   * never arrives as a failure.
   */
  SESSION_ENDED,
}

/** What went wrong, for logs. */
@ObjCEnum
enum class ReadFailureReason {
  /** The backend answered with GraphQL errors. */
  GRAPHQL_ERRORS,

  /** No answer arrived. */
  NETWORK,

  /** The backend answered with an HTTP error status. */
  HTTP,

  /** The caller waited as long as a read may take. */
  TIMEOUT,

  /** The same read is still waiting on an iOS app input, so this one was refused at once. */
  STUCK,

  /** A login or logout ended the read's session. */
  SESSION_ENDED,

  /** Anything else, a Kotlin bug included. */
  UNEXPECTED,
}

/** The failure a read raises when its data could not be loaded; [error] decides the reason. */
@HiddenFromObjC
fun generalReadFailure(error: ErrorMessage? = null): ReadFailure =
  ReadFailure(ReadFailureKind.GENERAL, reasonOf(error), "", "")

internal fun failure(kind: ReadFailureKind, reason: ReadFailureReason) = ReadFailure(kind, reason, "", "")

private fun reasonOf(error: ErrorMessage?): ReadFailureReason = when (val throwable = error?.throwable) {
  null -> if (error == null) ReadFailureReason.UNEXPECTED else ReadFailureReason.GRAPHQL_ERRORS
  is ApolloNetworkException -> ReadFailureReason.NETWORK
  is ApolloHttpException -> ReadFailureReason.HTTP
  else -> ReadFailureReason.UNEXPECTED
}
