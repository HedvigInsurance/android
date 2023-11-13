package com.hedvig.android.datadog.core

import timber.log.Timber

internal class DatadogLoggingTree(
  private val logger: com.datadog.android.log.Logger,
) : Timber.Tree() {
  override fun isLoggable(tag: String?, priority: Int): Boolean {
    return priority >= android.util.Log.DEBUG
  }

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
    logger.log(
      priority = priority,
      message = "$tagFormatted $message",
      throwable = t,
    )
  }
}
