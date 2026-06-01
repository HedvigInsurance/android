package com.hedvig.android.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.app.di.AppGraphHolder
import com.hedvig.android.notification.firebase.PushNotificationGraphProvider
import com.hedvig.android.notification.firebase.PushNotificationService
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject

@HasMemberInjections
open class HedvigApplication : Application(), PushNotificationGraphProvider {
  @Inject private lateinit var appInitializers: AppInitializers

  override fun onCreate() {
    super.onCreate()
    AppGraphHolder.graph.inject(this)
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    appInitializers.initialize()
  }

  override fun inject(service: PushNotificationService) {
    AppGraphHolder.graph.inject(service)
  }
}
