package com.hedvig.android.app.di

import android.content.Context
import com.hedvig.android.core.common.di.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {
  val workerFactory: MetroWorkerFactory

  @DependencyGraph.Factory
  interface Factory {
    fun create(
      @Provides applicationContext: Context,
    ): AppGraph
  }
}
