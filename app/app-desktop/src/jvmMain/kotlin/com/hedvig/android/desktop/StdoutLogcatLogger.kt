package com.hedvig.android.desktop

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import java.time.LocalTime

/** Writes every log line, and the stack trace of any throwable, to standard output. */
internal object StdoutLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    val line = "${LocalTime.now()} ${priority.name.first()}/${tag ?: "Hedvig"}: ${message()}"
    synchronized(this) {
      System.out.println(line)
      throwable?.printStackTrace(System.out)
    }
  }
}
