package com.hedvig.app.feature.tracking

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.hanalytics.engineering.tracking.TrackingShortcutService
import com.hedvig.app.feature.di.KoinInitializer
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import slimber.log.d

class TrackingInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    GlobalScope.launch {
      try {
        context.startService(TrackingShortcutService.newInstance(context))
        d { "TrackingInitializer started TrackingShortcutService" }
      } catch (ignored: Throwable) {
        // Ignore if the service can't start due to launch restrictions.
        d { "TrackingInitializer failed to start TrackingShortcutService" }
      }
    }
  }

  override fun dependencies() = listOf(KoinInitializer::class.java)
}
