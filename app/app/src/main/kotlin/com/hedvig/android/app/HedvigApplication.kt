package com.hedvig.android.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  private val appInitializers: AppInitializers by inject()

  override fun onCreate() {
    super.onCreate()
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    appInitializers.initialize()
  }
}
