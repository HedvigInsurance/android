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
  publicFunctionInLeafJvmModule()
  with(LogcatLogger.logger) {
    log(priority, throwable, message)
  }
}

fun publicFunctionInLeafJvmModule() {
  println("publicFunctionInLeafJvmModule + ${1 + 2}")
}
