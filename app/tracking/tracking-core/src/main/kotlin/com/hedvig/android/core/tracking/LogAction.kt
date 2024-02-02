package com.hedvig.android.core.tracking

@Suppress("NOTHING_TO_INLINE")
inline fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
  with(ActionLogger.actionLogger) {
    logAction(type, name, attributes)
  }
}

@Suppress("NOTHING_TO_INLINE")
inline fun logError(
  message: String,
  source: ErrorSource,
  attributes: Map<String, Any?> = emptyMap(),
  throwable: Throwable? = null,
  stacktrace: String? = null,
) {
  with(ActionLogger.actionLogger) {
    logError(message, source, attributes, throwable, stacktrace)
  }
}
