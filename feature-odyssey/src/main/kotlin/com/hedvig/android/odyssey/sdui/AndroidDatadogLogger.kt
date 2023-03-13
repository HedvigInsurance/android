package com.hedvig.android.odyssey.sdui

import com.hedvig.odyssey.datadog.DatadogLogger
import timber.log.Timber

internal class AndroidDatadogLogger : DatadogLogger {
  override fun debug(tag: String, message: String) {
    Timber.tag(tag).d(message)
  }

  override fun error(tag: String, message: String) {
    Timber.tag(tag).e(message)
  }

  override fun info(tag: String, message: String) {
    Timber.tag(tag).i(message)
  }

  override fun warn(tag: String, message: String) {
    Timber.tag(tag).w(message)
  }
}
