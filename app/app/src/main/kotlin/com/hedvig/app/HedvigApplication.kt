package com.hedvig.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.app.AppInitializers
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  private val languageService: LanguageService by inject()
  private val appInitializers: AppInitializers by inject()

  override fun onCreate() {
    super.onCreate()
    registerActivityLifecycleCallbacks(ActivityChangeTracker())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    languageService.performOnLaunchLanguageCheck()
    appInitializers.initialize()
  }
}
