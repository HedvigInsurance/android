package com.hedvig.android.app.firebase

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import timber.log.Timber

class FirebaseBreadcrumbTimberTree : Timber.Tree() {
  override fun isLoggable(tag: String?, priority: Int): Boolean {
    return priority >= android.util.Log.DEBUG
  }

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (t != null) return
    val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
    Firebase.crashlytics.log("${prefix(priority)} $tagFormatted $message")
  }

  private fun prefix(priority: Int) = buildString {
    append("[")
    append(
      when (priority) {
        2 -> "VERBOSE"
        3 -> "DEBUG"
        4 -> "INFO"
        5 -> "WARN"
        6 -> "ERROR"
        7 -> "ASSERT"
        else -> "UNKNOWN"
      },
    )
    append("]")
  }
}
