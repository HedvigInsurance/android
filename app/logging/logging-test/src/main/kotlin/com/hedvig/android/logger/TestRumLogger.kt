package com.hedvig.android.logger

import com.hedvig.android.core.tracking.ActionType
import com.hedvig.android.core.tracking.ErrorSource
import com.hedvig.android.core.tracking.RumLogger

internal class TestRumLogger : RumLogger {
  override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
    println("[$type] $name: $attributes")
  }

  override fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  ) {
    val throwableText = throwable?.let { " ${it.message}" } ?: ""
    println("[$source] $message: $throwableText | $stacktrace")
  }
}
