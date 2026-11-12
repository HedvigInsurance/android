package com.hedvig.android.desktop

import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.LogcatLogger
import com.hedvig.android.logger.logcat

/**
 * Installs the desktop app's loggers. Shared code reports actions and errors through [RumLogger], which throws
 * until a logger is installed, so this runs before anything else in the app.
 */
internal fun installDesktopLogging(logcatLogger: LogcatLogger) {
  LogcatLogger.install(logcatLogger)
  RumLogger.install(LogcatRumLogger)
}

/** Writes RUM actions and errors to [logcat], since the desktop app reports nothing to Datadog. */
internal object LogcatRumLogger : RumLogger {
  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
    logcat(LogPriority.DEBUG) { "RUM action $type $name attributes: $attributes" }
  }

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    logcat(LogPriority.WARN, throwable) { "RUM error from $source: $message attributes: $attributes" }
  }
}
