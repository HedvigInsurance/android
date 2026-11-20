package com.hedvig.android.umbrella

import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat

/**
 * Writes RUM actions and errors to [logcat], so they reach the iOS app's log sink. Attribute values can hold response
 * data, so only their keys are logged.
 */
internal object LogcatRumLogger : RumLogger {
  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
    logcat(LogPriority.DEBUG) { "RUM action $type $name attribute keys: ${attributes.keys}" }
  }

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    logcat(LogPriority.WARN, throwable) { "RUM error from $source: $message attribute keys: ${attributes.keys}" }
  }
}
