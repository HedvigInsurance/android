package com.hedvig.android.language

import android.app.Activity
import android.app.Application
import android.app.LocaleManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppLocalesMetadataHolderService
import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.initializable.Initializable
import com.hedvig.android.logger.logcat
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

/**
 * Prepares the stored language once per process, starting in `Application.onCreate` before any Activity exists: it
 * keeps AppCompat's one-time copy from wiping a language set in the system settings, and runs the one-time migration
 * of the value that earlier app versions stored on every launch.
 */
@ContributesIntoSet(AppScope::class)
@Inject
internal class LanguageStartup(
  private val context: Context,
  private val appLocaleStore: AppLocaleStore,
) : Initializable {
  override fun initialize() {
    markAppCompatSyncDoneIfFrameworkHasLocales()
    migrateStoredLanguageOnceAnActivityIsResumed()
  }

  /**
   * On API 33+ AppCompat copies its own stored locales into the framework once, on a background thread started by the
   * first Activity, and records the copy as done by enabling its AppLocalesMetadataHolderService component. Before an
   * Activity is alive it reads the framework value as empty, so it can write its own empty value over a language set
   * in the system settings before the first launch, or restored from a backup. When the framework already holds a
   * value there is nothing to copy, so the component is enabled here first and AppCompat skips the copy.
   */
  private fun markAppCompatSyncDoneIfFrameworkHasLocales() {
    if (Build.VERSION.SDK_INT < 33) return
    val packageManager = context.packageManager
    val appCompatSyncMarker = ComponentName(context, AppLocalesMetadataHolderService::class.java)
    if (packageManager.getComponentEnabledSetting(appCompatSyncMarker) ==
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    ) {
      return
    }
    val frameworkLocales = context.getSystemService(LocaleManager::class.java).applicationLocales
    if (frameworkLocales.isEmpty) return
    logcat {
      "LanguageStartup: framework already holds [${frameworkLocales.toLanguageTags()}], skipping AppCompat's copy"
    }
    packageManager.setComponentEnabledSetting(
      appCompatSyncMarker,
      PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
      PackageManager.DONT_KILL_APP,
    )
  }

  /**
   * Android keeps applying a cleared app locale to a process that had no Activity yet when it was cleared, which then
   * ignores changes to the phone's languages until it restarts. Clearing once an Activity is resumed avoids that.
   */
  private fun migrateStoredLanguageOnceAnActivityIsResumed() {
    // Read synchronously, still in Application.onCreate, to decide whether to wait for an Activity at all.
    val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    if (preferences.getBoolean(KEY_STORED_LANGUAGE_MIGRATED, false)) return
    val application = context.applicationContext as Application
    application.registerActivityLifecycleCallbacks(
      object : Application.ActivityLifecycleCallbacks {
        override fun onActivityResumed(activity: Activity) {
          application.unregisterActivityLifecycleCallbacks(this)
          migrateStoredLanguage(preferences)
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {}

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
      },
    )
  }

  private fun migrateStoredLanguage(preferences: SharedPreferences) {
    val storedLocaleTags = appLocaleStore.storedLocaleTags()
    val phoneLocaleTags = appLocaleStore.phoneLocaleTags()
    val migration = storedLanguageMigration(storedLocaleTags, phoneLocaleTags)
    val migratedLocaleTags = when (migration) {
      StoredLanguageMigration.Keep -> storedLocaleTags
      StoredLanguageMigration.Clear -> appLocaleStore.localeTagsToFollowPhone()
    }
    logcat {
      "LanguageStartup: stored [${storedLocaleTags.joinToString()}], phone [${phoneLocaleTags.joinToString()}], " +
        "$migration to [${migratedLocaleTags.joinToString()}]"
    }
    if (migratedLocaleTags != storedLocaleTags) {
      appLocaleStore.store(migratedLocaleTags)
    }
    preferences.edit().putBoolean(KEY_STORED_LANGUAGE_MIGRATED, true).apply()
  }

  private companion object {
    const val PREFERENCES_NAME = "language_startup"
    const val KEY_STORED_LANGUAGE_MIGRATED = "stored_language_migrated"
  }
}
