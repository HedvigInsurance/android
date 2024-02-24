package com.hedvig.android.language

import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.hedvig.android.logger.LogPriority
import com.hedvig.android.logger.logcat
import java.util.Locale

interface LanguageService {
  @MainThread
  fun setLanguage(language: Language)

  /**
   * Returns the language that was selected by the user, or null if no language was manually selected.
   */
  fun getSelectedLanguage(): Language?

  fun getLanguage(): Language

  fun getLocale(): Locale
}

internal class AndroidLanguageService() : LanguageService {
  /**
   * Sets the language, and as a side effect, restarts all running activities.
   * Only safe to call from the Main Thread.
   */
  @MainThread
  override fun setLanguage(language: Language) {
    logcat { "LanguageService: setLanguage: $language" }
    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.toString()))
  }

  override fun getSelectedLanguage(): Language? {
    val locale = getSelectedLocale() ?: return null
    return Language.from(locale.toLanguageTag())
  }

  override fun getLanguage(): Language {
    return getSelectedLanguage() ?: let {
      logcat(LogPriority.WARN) { "LanguageService: getLocale: No locale set, defaulting to en_SE" }
      Language.EN_SE
    }
  }

  override fun getLocale(): Locale {
    return getSelectedLocale() ?: Locale("en", "SE")
  }

  private fun getSelectedLocale(): Locale? {
    return getLocaleFromAppCompat()
  }

  private fun getLocaleFromAppCompat(): Locale? {
    val localeList = AppCompatDelegate.getApplicationLocales()
    return if (localeList.isEmpty) {
      null
    } else {
      localeList[0]!!
    }
  }
}
