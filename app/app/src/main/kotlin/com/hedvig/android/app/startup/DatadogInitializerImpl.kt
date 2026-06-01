package com.hedvig.android.app.startup

import androidx.startup.Initializer
import com.hedvig.android.app.di.AppGraphHolder
import com.hedvig.android.app.di.AppGraphInitializer
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.datadog.core.DatadogInitializer

@Suppress("unused") // Used in /app/src/main/AndroidManifest.xml
class DatadogInitializerImpl : DatadogInitializer() {
  override val hedvigBuildConstants: HedvigBuildConstants
    get() = AppGraphHolder.graph.hedvigBuildConstants

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(TimberInitializer::class.java, AppGraphInitializer::class.java)
  }
}
