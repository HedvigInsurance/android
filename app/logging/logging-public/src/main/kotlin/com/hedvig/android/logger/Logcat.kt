package com.hedvig.android.logger

/**
 * The main entrypoint to logging in the entire app.
 */
@Suppress("NOTHING_TO_INLINE")
inline fun logcat(
  priority: LogPriority = LogPriority.DEBUG,
  throwable: Throwable? = null,
  noinline message: () -> String,
) {
  with(LogcatLogger.logger) {
    log(priority, throwable, message)
  }
}
