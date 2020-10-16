package com.hedvig.app.util

import android.util.Log
import timber.log.Timber
import com.google.firebase.crashlytics.FirebaseCrashlytics

class FirebaseCrashlyticsLogExceptionTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int) = priority >= Log.ERROR

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        t?.let { FirebaseCrashlytics.getInstance().recordException(it) } ?: run {
            val tagFormatted = tag?.let { "[$tag]" }.orEmpty()
            FirebaseCrashlytics.getInstance().log("${prefix(priority)}${tagFormatted}${message}")
        }
    }

    fun prefix(priority: Int) = when (priority) {
        Log.ERROR -> "[ERROR]"
        else -> "[UNKNOWN($priority)]"
    }
}
