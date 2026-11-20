package com.hedvig.android.logger

import platform.Foundation.NSLog

/**
 * Where the iOS app takes Kotlin's log lines, for example to forward them to its own logger. Implemented in Swift and
 * installed with [IosLogcatLogger.Companion.install]. Called on whichever thread logged the line.
 */
interface IosLogSink {
  /**
   * One line at [priority] from [tag]. A throwable logged with the line is named by its class only, since its message
   * can quote what was sent.
   */
  fun log(priority: LogPriority, tag: String?, message: String)
}

/** Writes every line at [minimumPriority] or above to [write], and drops the rest without building their message. */
class IosLogcatLogger internal constructor(
  private val minimumPriority: LogPriority,
  private val write: (priority: LogPriority, tag: String?, message: String) -> Unit,
) : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    if (priority < minimumPriority) return
    val throwablePart = if (throwable != null) " (${throwable::class.simpleName})" else ""
    write(priority, tag, message() + throwablePart)
  }

  companion object {
    /** Installs a logger writing every line, with its stack trace, to `NSLog`, unless a logger is installed. */
    fun install() {
      if (!LogcatLogger.isInstalled) {
        LogcatLogger.install(NSLogLogger)
      }
    }

    /**
     * Sends every line at [minimumPriority] or above to [sink], replacing the logger installed before, if any. Call it
     * before anything else in Kotlin logs, so no line goes to `NSLog` instead.
     */
    fun install(sink: IosLogSink, minimumPriority: LogPriority) {
      LogcatLogger.replace(IosLogcatLogger(minimumPriority, sink::log))
    }
  }
}

private object NSLogLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val throwablePart = if (throwable != null) "\n${throwable.stackTraceToString()}" else ""
    nsLog(priority, tag, message() + throwablePart)
  }
}

private fun nsLog(priority: LogPriority, tag: String?, message: String) {
  val prefix = when (priority) {
    LogPriority.VERBOSE -> "V"
    LogPriority.DEBUG -> "D"
    LogPriority.INFO -> "I"
    LogPriority.WARN -> "W"
    LogPriority.ERROR -> "E"
    LogPriority.ASSERT -> "A"
  }
  val tagPart = if (tag != null) "[$tag] " else ""
  NSLog("%s", "$prefix/$tagPart$message")
}
