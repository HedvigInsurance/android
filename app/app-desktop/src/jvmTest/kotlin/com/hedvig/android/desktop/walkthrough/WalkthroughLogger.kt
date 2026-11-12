package com.hedvig.android.desktop.walkthrough

import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger

/** One WARN or ERROR line, with the first frames of its throwable. */
internal data class LoggedProblem(val priority: LogPriority, val tag: String?, val message: String, val trace: String?)

/**
 * Keeps the WARN and ERROR lines logged since the last [drain], and also prints every line of INFO and above, so a
 * run's console shows what the app logged.
 */
internal class WalkthroughLogger : LogcatLogger {
  private val problems = mutableListOf<LoggedProblem>()

  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    if (priority < LogPriority.INFO) return
    val text = message()
    println("${priority.name.first()}/${tag ?: "Hedvig"}: $text")
    if (priority >= LogPriority.WARN) {
      synchronized(problems) {
        problems += LoggedProblem(priority, tag, text, throwable?.firstFrames())
      }
    }
  }

  fun drain(): List<LoggedProblem> = synchronized(problems) { problems.toList().also { problems.clear() } }
}

/** The throwable's class, message and first [count] frames, followed by the same for its causes. */
internal fun Throwable.firstFrames(count: Int = 6): String = buildString {
  var current: Throwable? = this@firstFrames
  var depth = 0
  while (current != null && depth < 3) {
    if (depth > 0) append("\nCaused by: ")
    append(current::class.qualifiedName).append(": ").append(current.message?.take(300))
    current.stackTrace.take(count).forEach { append("\n  at ").append(it) }
    current = current.cause.takeIf { it !== current }
    depth++
  }
}
