package com.hedvig.app.feature.tracking

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.startup.Initializer
import com.hedvig.app.feature.di.KoinInitializer
import com.hedvig.hanalytics.HAnalytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@Suppress("unused") // Used in /app/src/main/AndroidManifest.xml
class LaunchTrackingInitializer : Initializer<Unit>, KoinComponent {
  private val hAnalytics: HAnalytics by inject()
  override fun create(context: Context) {
    hAnalytics.identify()
    hAnalytics.notificationPermission(
      NotificationManagerCompat.from(context).areNotificationsEnabled(),
    )
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(KoinInitializer::class.java)
  }
}
