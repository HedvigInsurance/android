package com.hedvig.android.app.firebase

import android.util.Log
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
import timber.log.Timber

class FirebaseCrashlyticsLogExceptionTree : Timber.Tree() {
  override fun isLoggable(tag: String?, priority: Int) = priority >= Log.ERROR

  override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
    if (t != null) {
      Firebase.crashlytics.recordException(t)
    } else {
      val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
      Firebase.crashlytics.log("${prefix(priority)}${tagFormatted}$message")
    }
  }

  private fun prefix(priority: Int) = when (priority) {
    Log.ERROR -> "[ERROR]"
    else -> "[UNKNOWN($priority)]"
  }
}
