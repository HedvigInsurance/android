package com.hedvig.android.core.tracking

import com.hedvig.android.logger.logcat

interface RumLogger {
  fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>)

  fun logError(
    message: String,
    source: ErrorSource,
    attributes: Map<String, Any?>,
    throwable: Throwable?,
    stacktrace: String?,
  )

  companion object {
    @Volatile
    @PublishedApi
    internal var rumLogger: RumLogger = NoLog
      private set

    @Volatile
    private var installedThrowable: Throwable? = null

    val isInstalled: Boolean
      get() = installedThrowable != null

    fun install(rumLogger: RumLogger) {
      synchronized(this) {
        if (isInstalled) {
          logcat { "Installing $rumLogger even though an action logger was previously installed" }
        }
        installedThrowable = RuntimeException("Previous logger installed here")
        this.rumLogger = rumLogger
      }
    }
  }

  private object NoLog : RumLogger {
    override fun logAction(type: ActionType, name: String, attributes: Map<String, Any?>) {
      error("Should never receive any action")
    }

    override fun logError(
      message: String,
      source: ErrorSource,
      attributes: Map<String, Any?>,
      throwable: Throwable?,
      stacktrace: String?,
    ) {
      error("Should never receive any error")
    }
  }
}

enum class ActionType {
  /** User tapped on a widget. */
  TAP,

  /** User scrolled a view. */
  SCROLL,

  /** User swiped on a view. */
  SWIPE,

  /** User clicked on a widget (not used on Mobile). */
  CLICK,

  /** User navigated back. */
  BACK,

  /** A custom action. */
  CUSTOM,
}

enum class ErrorSource {
  /** Error originated in the Network layer. */
  NETWORK,

  /** Error originated in the source code (usually a crash). */
  SOURCE,

  /** Error extracted from a logged error. */
  LOGGER,
}
