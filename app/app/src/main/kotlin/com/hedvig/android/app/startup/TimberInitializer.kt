package com.hedvig.android.app.startup

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.firebase.FirebaseBreadcrumbTimberTree
import com.hedvig.android.app.firebase.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.android.logger.AndroidLogcatLogger
import com.hedvig.app.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (shouldIncludeDebugLoggingTree()) {
      Timber.plant(Timber.DebugTree())
    }
    Timber.plant(FirebaseBreadcrumbTimberTree())
    Timber.plant(FirebaseCrashlyticsLogExceptionTree())
    AndroidLogcatLogger.install()
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return emptyList()
  }
}

private fun shouldIncludeDebugLoggingTree() = BuildConfig.BUILD_TYPE == "debug" ||
  BuildConfig.APPLICATION_ID == "staging" ||
  BuildConfig.DEBUG
