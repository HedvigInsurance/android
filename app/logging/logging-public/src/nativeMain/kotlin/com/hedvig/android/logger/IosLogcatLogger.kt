package com.hedvig.android.logger

import platform.Foundation.NSLog

class IosLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
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
    NSLog("%s", "$prefix/$tagPart$messagePart$throwablePart")
  }

  companion object {
    fun install() {
      if (!LogcatLogger.isInstalled) {
        LogcatLogger.install(IosLogcatLogger())
      }
    }
  }
}
