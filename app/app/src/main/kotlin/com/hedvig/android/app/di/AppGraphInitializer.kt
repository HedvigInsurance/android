package com.hedvig.android.app.di

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.HedvigApplication
import com.hedvig.android.app.startup.TimberInitializer
import dev.zacsweers.metro.createGraphFactory

class AppGraphInitializer : Initializer<Unit> {
  override fun create(context: Context) {
    val application = context.applicationContext as HedvigApplication
    application.appGraph = createGraphFactory<AppGraph.Factory>().create(context.applicationContext)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> = listOf(TimberInitializer::class.java)
}
