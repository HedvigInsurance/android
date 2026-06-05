package com.hedvig.android.app.di

import android.content.Context
import com.hedvig.android.app.HedvigApplication
import com.hedvig.android.app.MainActivity
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.notification.firebase.PushNotificationService
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlinx.serialization.modules.SerializersModule

@DependencyGraph(AppScope::class)
internal interface AppGraph : ViewModelGraph {
  val workerFactory: MetroWorkerFactory

  val hedvigBuildConstants: HedvigBuildConstants

  /**
   * Per-feature [SerializersModule]s registering each module's [com.hedvig.android.navigation.common.HedvigNavKey]
   * subtypes for back-stack process-death persistence. Allowed to be empty so the graph builds before any feature
   * contributes (a logged-out cold start only needs the key feature-login registers).
   */
  @Multibinds(allowEmpty = true)
  val serializersModules: Set<SerializersModule>

  fun inject(activity: MainActivity)

  fun inject(application: HedvigApplication)

  fun inject(service: PushNotificationService)

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides applicationContext: Context,
    ): AppGraph
  }
}
