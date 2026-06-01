package com.hedvig.android.app.di

import android.content.Context
import com.hedvig.android.app.HedvigApplication
import com.hedvig.android.app.MainActivity
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.notification.firebase.PushNotificationService
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
internal interface AppGraph : ViewModelGraph {
  val workerFactory: MetroWorkerFactory

  val hedvigBuildConstants: HedvigBuildConstants

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
