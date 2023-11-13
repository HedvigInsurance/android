package com.hedvig.android.language

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

interface LanguageService {
  @MainThread
  fun setLanguage(language: Language)

  fun getLanguage(): Language

  fun getLocale(): java.util.Locale

  fun performOnLaunchLanguageCheck()
}

class AndroidLanguageService() : LanguageService {
  /**
   * Sets the language, and as a side effect, restarts all running activities.
   * Only safe to call from the Main Thread.
   */
  @MainThread
  override fun setLanguage(language: Language) {
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.toString()))
  }

  override fun getLanguage(): Language {
    return Language.from(getLocale().toLanguageTag())
  }

  override fun getLocale(): Locale {
    val localeList = AppCompatDelegate.getApplicationLocales()
    if (localeList.isEmpty) {
      return Locale("en", "SE")
    }
    return localeList[0]!!
  }

  override fun performOnLaunchLanguageCheck() {
    val currentLanguage = AppCompatDelegate.getApplicationLocales()
    // Always default to English, Sweden, if nothing else is set
    if (currentLanguage.isEmpty) {
      setLanguage(Language.EN_SE)
    }
  }
}
