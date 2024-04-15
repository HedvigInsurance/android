package com.hedvig.app.startup

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.firebase.FirebaseBreadcrumbTimberTree
import com.hedvig.android.app.firebase.FirebaseCrashlyticsLogExceptionTree
import com.hedvig.android.logger.AndroidLogcatLogger
import com.hedvig.app.BuildConfig
import timber.log.Timber

class TimberInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    if (isDebug()) {
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

private fun isDebug() = BuildConfig.APPLICATION_ID == "com.hedvig.dev.app" ||
  BuildConfig.APPLICATION_ID == "com.hedvig.test.app" ||
  BuildConfig.DEBUG
