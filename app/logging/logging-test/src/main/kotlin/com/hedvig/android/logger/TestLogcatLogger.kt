package com.hedvig.android.logger

/**
 * A test [LogcatLogger] implementation which just prints out to [System.out].
 */
class TestLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, message: () -> String) {
    val throwableText = throwable?.let { " ${it.message}" } ?: ""
    println("[${priority.name}] ${message()}" + throwableText)
  }
}
