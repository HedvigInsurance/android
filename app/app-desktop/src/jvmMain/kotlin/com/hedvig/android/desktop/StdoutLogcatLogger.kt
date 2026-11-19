package com.hedvig.android.desktop

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import java.io.PrintStream
import java.time.LocalTime

/** Writes every log line at [minPriority] or above, and the stack trace of any throwable, to [output]. */
internal class StdoutLogcatLogger(
  private val minPriority: LogPriority,
  private val output: PrintStream = System.out,
) : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    if (priority < minPriority) return
    val line = "${LocalTime.now()} ${priority.name.first()}/${tag ?: "Hedvig"}: ${message()}"
    synchronized(this) {
      output.println(line)
      throwable?.printStackTrace(output)
    }
  }
}

/**
 * The standard output logger for [backend]. Against production it drops DEBUG and VERBOSE lines, since those carry
 * network response bodies with real members' data; against staging it writes every line.
 */
internal fun stdoutLogcatLogger(backend: DesktopBackend, output: PrintStream = System.out): StdoutLogcatLogger {
  val minPriority = when (backend) {
    DesktopBackend.Staging -> LogPriority.VERBOSE
    DesktopBackend.Production -> LogPriority.INFO
  }
  return StdoutLogcatLogger(minPriority, output)
}
