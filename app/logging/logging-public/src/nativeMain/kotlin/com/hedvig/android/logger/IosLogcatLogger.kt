package com.hedvig.android.logger

import platform.Foundation.NSLog

/** Writes every line at [minimumPriority] or above to [write], and drops the rest without building their message. */
class IosLogcatLogger internal constructor(
  private val minimumPriority: LogPriority,
  private val write: (String) -> Unit,
) : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    if (priority < minimumPriority) return
    val prefix = when (priority) {
      LogPriority.VERBOSE -> "V"
      LogPriority.DEBUG -> "D"
      LogPriority.INFO -> "I"
      LogPriority.WARN -> "W"
      LogPriority.ERROR -> "E"
      LogPriority.ASSERT -> "A"
    }
    val tagPart = if (tag != null) "[$tag] " else ""
    val messagePart = message()
    val throwablePart = if (throwable != null) "\n${throwable.stackTraceToString()}" else ""
    write("$prefix/$tagPart$messagePart$throwablePart")
  }

  companion object {
    /** Installs a logger writing lines at [minimumPriority] or above to `NSLog`, unless a logger is installed. */
    fun install(minimumPriority: LogPriority) {
      if (!LogcatLogger.isInstalled) {
        LogcatLogger.install(IosLogcatLogger(minimumPriority) { NSLog("%s", it) })
      }
    }
  }
}
