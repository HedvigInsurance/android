package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.app.AppInitializers
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  private val appInitializers: AppInitializers by inject()

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(ActivityChangeTracker())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    appInitializers.initialize()
  }
}
