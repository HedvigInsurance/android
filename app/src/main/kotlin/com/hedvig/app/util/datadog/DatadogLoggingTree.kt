package com.hedvig.app.util.datadog

import timber.log.Timber

class DatadogLoggingTree : Timber.Tree() {
  private val datadogLogger = com.datadog.android.log.Logger.Builder()
    .setNetworkInfoEnabled(true)
    .build()

  override fun isLoggable(tag: String?, priority: Int): Boolean {
    return priority >= android.util.Log.DEBUG
  }

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
    datadogLogger.log(
      priority = priority,
      message = "$tagFormatted $message",
      throwable = t,
    )
  }
}
