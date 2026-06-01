package com.hedvig.android.app.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.startup.TimberInitializer
import com.hedvig.android.core.common.di.MetroGraphHolder
import dev.zacsweers.metro.createGraphFactory

class AppGraphInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val graph = createGraphFactory<AppGraph.Factory>().create(context.applicationContext)
    AppGraphHolder.graph = graph
    MetroGraphHolder.graph = graph
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = listOf(TimberInitializer::class.java)
}

internal object AppGraphHolder {
  lateinit var graph: AppGraph
}
