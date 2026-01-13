package com.hedvig.android.logger

import com.hedvig.android.apollo.ApolloOperationError

/**
 * The main entrypoint to logging in the entire app.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun logcat(
  priority: LogPriority = LogPriority.DEBUG,
  throwable: Throwable? = null,
  tag: String? = null,
  noinline message: () -> String,
) {
  with(LogcatLogger.logger) {
    log(priority, throwable, tag, message)
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun logcat(
  priority: LogPriority = LogPriority.DEBUG,
  operationError: ApolloOperationError,
  tag: String? = null,
  noinline message: () -> String,
) {
  val adjustedPriority = if (operationError.containsUnauthenticatedError) {
    priority.atMost(LogPriority.WARN)
  } else {
    priority
  }
  logcat(adjustedPriority, operationError.throwable, tag, message)
}
