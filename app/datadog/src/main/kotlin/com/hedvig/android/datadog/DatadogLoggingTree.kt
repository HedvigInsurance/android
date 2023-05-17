package com.hedvig.android.datadog

import timber.log.Timber

/**
 * To filter specifically for only these logs locally, to emulate what one would see in datadog logs, use the following
 * logcat filter:
 * package:mine tag=:android
 *
 * This filters for only our package, plus *exactly* the "android" tag.
 */
internal class DatadogLoggingTree : Timber.Tree() {
  private val datadogLogger = com.datadog.android.log.Logger.Builder()
    .setNetworkInfoEnabled(true)
    .setDatadogLogsMinPriority(android.util.Log.DEBUG)
    .setLogcatLogsEnabled(true)
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
