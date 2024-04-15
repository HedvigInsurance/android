package com.hedvig.app.startup

import androidx.startup.Initializer
import com.hedvig.android.app.di.KoinInitializer
import com.hedvig.android.datadog.core.DatadogInitializer

@Suppress("unused") // Used in /app/src/main/AndroidManifest.xml
class DatadogInitializerImpl : DatadogInitializer() {
  override fun dependencies(): List<Class<out Initializer<*>>> {
    return listOf(TimberInitializer::class.java, KoinInitializer::class.java)
  }
}
