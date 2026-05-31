package com.hedvig.android.app.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.startup.TimberInitializer
import dev.zacsweers.metro.createGraphFactory

class AppGraphInitializer : Initializer<AppGraph> {
  override fun create(context: Context): AppGraph {
    val graph = createGraphFactory<AppGraph.Factory>().create(context.applicationContext)
    AppGraphHolder.graph = graph
    return graph
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = listOf(TimberInitializer::class.java)
}

object AppGraphHolder {
  lateinit var graph: AppGraph
}
