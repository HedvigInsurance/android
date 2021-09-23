package com.hedvig.app.util

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class FirebaseCrashlyticsLogExceptionTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int) = priority >= Log.ERROR

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (t != null) {
            FirebaseCrashlytics.getInstance().recordException(t)
        } else {
            val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
            FirebaseCrashlytics.getInstance().log("${prefix(priority)}${tagFormatted}$message")
        }
    }

    private fun prefix(priority: Int) = when (priority) {
        Log.ERROR -> "[ERROR]"
        else -> "[UNKNOWN($priority)]"
    }
}
