package com.hedvig.android.logger

import slimber.log.d
import slimber.log.e
import slimber.log.i
import slimber.log.v
import slimber.log.w
import slimber.log.wtf
import timber.log.Timber

/**
 * A [LogcatLogger] logger that delegates to [slimber].
 *
 * The implementation is based on [square logcat](https://github.com/square/logcat).
 */
class AndroidLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, tag: String?, message: () -> String) {
    when (priority) {
      LogPriority.VERBOSE -> v(throwable, tag, message)
      LogPriority.DEBUG -> d(throwable, tag, message)
      LogPriority.INFO -> i(throwable, tag, message)
      LogPriority.WARN -> w(throwable, tag, message)
      LogPriority.ERROR -> e(throwable, tag, message)
      LogPriority.ASSERT -> wtf(throwable, tag, message)
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

private fun v(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    v(throwable, message)
  } else {
    v(message)
  }
}

private fun d(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    d(throwable, message)
  } else {
    d(message)
  }
}

private fun i(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    i(throwable, message)
  } else {
    i(message)
  }
}

private fun w(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    w(throwable, message)
  } else {
    w(message)
  }
}

private fun e(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    e(throwable, message)
  } else {
    e(message)
  }
}

private fun wtf(throwable: Throwable?, tag: String?, message: () -> String) {
  if (tag != null) {
    Timber.tag(tag)
  }
  if (throwable != null) {
    wtf(throwable, message)
  } else {
    wtf(message)
  }
}
