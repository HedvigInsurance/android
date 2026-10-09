package com.hedvig.android.logger

import timber.log.Timber

/**
 * A [LogcatLogger] logger that delegates to [Timber].
 *
 * The implementation is based on [square logcat](https://github.com/square/logcat).
 */
class AndroidLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    // Skip building the message when there is no tree to receive it
    if (Timber.treeCount == 0) return
    if (tag != null) {
      Timber.tag(tag)
    }
    val text = message()
    when (priority) {
      LogPriority.VERBOSE -> Timber.v(throwable, text)
      LogPriority.DEBUG -> Timber.d(throwable, text)
      LogPriority.INFO -> Timber.i(throwable, text)
      LogPriority.WARN -> Timber.w(throwable, text)
      LogPriority.ERROR -> Timber.e(throwable, text)
      LogPriority.ASSERT -> Timber.wtf(throwable, text)
    }
  }

  companion object {
    fun install() {
      if (!LogcatLogger.isInstalled) {
        LogcatLogger.install(AndroidLogcatLogger())
      }
    }
  }
}
