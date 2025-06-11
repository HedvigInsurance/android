package com.hedvig.android.core.tracking

@Suppress("NOTHING_TO_INLINE")
inline fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
  with(RumLogger.rumLogger) {
    logAction(type, name, attributes)
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun logError(
  message: String,
  source: ErrorSource,
  attributes: Map<String, Any?> = emptyMap(),
  throwable: Throwable? = null,
  stacktrace: String? = throwable?.stackTraceToString(),
) {
  with(RumLogger.rumLogger) {
    logError(message, source, attributes, throwable, stacktrace)
  }
}
