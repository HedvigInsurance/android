package com.hedvig.android.logger

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
    slimber.log.v(throwable, message)
  } else {
    slimber.log.v(message)
  }
}

private fun d(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    slimber.log.d(throwable, message)
  } else {
    slimber.log.d(message)
  }
}

private fun i(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    slimber.log.i(throwable, message)
  } else {
    slimber.log.i(message)
  }
}

private fun w(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    slimber.log.w(throwable, message)
  } else {
    slimber.log.w(message)
  }
}

private fun e(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    slimber.log.e(throwable, message)
  } else {
    slimber.log.e(message)
  }
}

private fun wtf(throwable: Throwable?, message: () -> String) {
  if (throwable != null) {
    slimber.log.wtf(throwable, message)
  } else {
    slimber.log.wtf(message)
  }
}
