package com.hedvig.app

import android.content.Context
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat
import androidx.preference.PreferenceManager
import com.hedvig.android.core.common.preferences.PreferenceKey
import com.hedvig.android.market.Language
import com.hedvig.android.market.MarketManager

class LanguageService(
  private val context: Context,
  private val marketManager: MarketManager,
) {
  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

  /**
   * Sets the language, and as a side effect, restarts all running activities.
   * Only safe to call from the Main Thread.
   */
  @MainThread
  fun setLanguage(language: Language) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.toString()))
  }

  fun getLanguage(): Language? {
    val localeList = AppCompatDelegate.getApplicationLocales()
    if (localeList.isEmpty) {
      return null
    }
    val locale = localeList[0]!!
    return Language.from(locale.toLanguageTag())
  }

  fun performOnLaunchLanguageCheck() {
    val currentLanguage = AppCompatDelegate.getApplicationLocales()
    // Always default to English, Sweden, if nothing else is set
    if (currentLanguage.isEmpty) {
      setLanguage(Language.EN_SE)
    }
  }

  // Call this from SplashActivity
  fun performOneTimeLanguageMigration() {
    // We're already migrated, and hence we don't need to do anything
    if (preferences.contains(PreferenceKey.SETTING_LANGUAGE).not()) {
      return
    }
    // Retrieve the language from settings
    val storedLanguage = Language.fromSettings(context, marketManager.market)
    // Set it using official APIs
    setLanguage(storedLanguage)
    // Clear it out from storage
    preferences.edit {
      remove(PreferenceKey.SETTING_LANGUAGE)
    }
  }
}
