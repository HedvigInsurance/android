package com.hedvig.app

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ProcessLifecycleOwner
import com.hedvig.android.data.settings.datastore.SettingsDataStore
import com.hedvig.android.hanalytics.android.tracking.ApplicationLifecycleTracker
import com.hedvig.android.language.LanguageService
import com.hedvig.android.theme.Theme
import com.hedvig.app.feature.tracking.ActivityChangeTracker
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

open class HedvigApplication : Application() {
  private val applicationLifecycleTracker: ApplicationLifecycleTracker by inject()
  private val languageService: LanguageService by inject()
  private val settingsDataStore: SettingsDataStore by inject()

  override fun onCreate() {
    super.onCreate()
    ProcessLifecycleOwner.get().lifecycle.addObserver(applicationLifecycleTracker)
    MainScope().launch {
      settingsDataStore.observeTheme().first().apply()
    }
    registerActivityLifecycleCallbacks(ActivityChangeTracker())
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    languageService.performOnLaunchLanguageCheck()
  }
}

private fun Theme.apply() = when (this) {
  Theme.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
  Theme.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
  Theme.SYSTEM_DEFAULT -> {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY)
    }
  }
}
