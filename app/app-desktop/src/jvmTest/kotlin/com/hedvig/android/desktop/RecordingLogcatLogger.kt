package com.hedvig.android.desktop

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger

/** Keeps every log line, for tests that assert what was logged. */
internal class RecordingLogcatLogger : LogcatLogger {
  val lines = mutableListOf<Pair<LogPriority, String>>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    lines += priority to message()
  }
}
