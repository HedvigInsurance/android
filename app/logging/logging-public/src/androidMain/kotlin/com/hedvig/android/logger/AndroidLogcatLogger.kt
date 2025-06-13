package com.hedvig.android.logger

import slimber.log.d
import slimber.log.e
import slimber.log.i
import slimber.log.v
import slimber.log.w
import slimber.log.wtf

/**
 * A [LogcatLogger] logger that delegates to [slimber].
 *
 * The implementation is based on [square logcat](https://github.com/square/logcat).
 */
class AndroidLogcatLogger : LogcatLogger {
  override fun log(priority: LogPriority, throwable: Throwable?, message: () -> String) {
    when (priority) {
      LogPriority.VERBOSE -> v(throwable, message)
      LogPriority.DEBUG -> d(throwable, message)
      LogPriority.INFO -> i(throwable, message)
      LogPriority.WARN -> w(throwable, message)
      LogPriority.ERROR -> e(throwable, message)
      LogPriority.ASSERT -> wtf(throwable, message)
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

private fun v(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    v(throwable, message)
  } else {
    v(message)
  }
}

private fun d(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    d(throwable, message)
  } else {
    d(message)
  }
}

private fun i(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    i(throwable, message)
  } else {
    i(message)
  }
}

private fun w(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    w(throwable, message)
  } else {
    w(message)
  }
}

private fun e(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    e(throwable, message)
  } else {
    e(message)
  }
}

private fun wtf(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    wtf(throwable, message)
  } else {
    wtf(message)
  }
}
