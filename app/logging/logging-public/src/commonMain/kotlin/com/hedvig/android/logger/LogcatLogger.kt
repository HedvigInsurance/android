package com.hedvig.android.logger

import kotlin.concurrent.Volatile
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized

/**
 * Logger that [logcat] delegates to. Call [install] to install a new logger, the default is a
 * no-op logger. Calling [uninstall] falls back to the default no-op logger.
 */
interface LogcatLogger {
  /**
   * Write a log to its destination. Called by [logcat].
   */
  fun log(priority: LogPriority, throwable: Throwable?, message: () -> String)

  companion object : SynchronizedObject() {
    @Volatile
    @PublishedApi
    internal var logger: LogcatLogger = NoLog
      private set

    @Volatile
    private var installedThrowable: Throwable? = null

    val isInstalled: Boolean
      get() = installedThrowable != null

    /**
     * Installs a [LogcatLogger].
     *
     * It is an error to call [install] more than once however doing this won't throw, it'll log an error to the newly
     * provided logger.
     */
    fun install(logger: LogcatLogger) {
      synchronized(this) {
        if (isInstalled) {
          logger.log(LogPriority.ERROR, installedThrowable) {
            "Installing $logger even though a logger was previously installed"
          }
        }
        installedThrowable = RuntimeException("Previous logger installed here")
        this.logger = logger
      }
    }

    /**
     * Replaces the current logger (if any) with a no-op logger.
     */
    fun uninstall() {
      synchronized(this) {
        installedThrowable = null
        logger = NoLog
      }
    }
  }

  /**
   * If this fails a test, consider adding [com.hedvig.android.logger.TestLogcatLoggingRule] test rule to your test.
   */
  private object NoLog : LogcatLogger {
    override fun log(priority: LogPriority, throwable: Throwable?, message: () -> String) =
      error("Should never receive any log")
  }
}
