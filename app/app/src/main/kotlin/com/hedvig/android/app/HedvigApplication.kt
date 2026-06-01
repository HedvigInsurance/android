package com.hedvig.android.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.app.di.AppGraph
import com.hedvig.android.core.common.di.MetroGraphProvider
import com.hedvig.android.notification.firebase.PushNotificationGraphProvider
import com.hedvig.android.notification.firebase.PushNotificationService
import dev.zacsweers.metro.HasMemberInjections
import dev.zacsweers.metro.Inject

@HasMemberInjections
open class HedvigApplication :
  Application(),
  PushNotificationGraphProvider,
  MetroGraphProvider {
  internal lateinit var appGraph: AppGraph

  override val metroGraph: Any
    get() = appGraph

  @Inject private lateinit var appInitializers: AppInitializers

  override fun onCreate() {
    super.onCreate()
    appGraph.inject(this)
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    appInitializers.initialize()
  }

  override fun inject(service: PushNotificationService) {
    appGraph.inject(service)
  }
}
