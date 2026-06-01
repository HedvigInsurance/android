package com.hedvig.android.app.startup

import android.content.Context
import androidx.startup.Initializer
import com.hedvig.android.app.HedvigApplication
import com.hedvig.android.app.di.AppGraphInitializer
import com.hedvig.android.core.buildconstants.HedvigBuildConstants
import com.hedvig.android.datadog.core.DatadogInitializer

@Suppress("unused") // Used in /app/src/main/AndroidManifest.xml
class DatadogInitializerImpl : DatadogInitializer() {
  private lateinit var application: HedvigApplication

  override val hedvigBuildConstants: HedvigBuildConstants
    get() = application.appGraph.hedvigBuildConstants

  override fun create(context: Context) {
    application = context.applicationContext as HedvigApplication
    super.create(context)
  }

  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(TimberInitializer::class.java, AppGraphInitializer::class.java)
  }
}
