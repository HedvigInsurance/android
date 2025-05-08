package com.hedvig.android.language

import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.logger.logcat
import java.util.Locale

/**
 * Runs once on app startup to ensure that EN or SE languages are properly set on app startup
 * https://developer.android.com/guide/topics/resources/app-languages#impl-overview
 */
interface LanguageLaunchCheckUseCase {
  fun invoke(defLocale: Locale)
}

internal class AndroidLanguageLaunchCheckUseCase(
  private val languageService: LanguageService,
) : LanguageLaunchCheckUseCase {
  override fun invoke(defLocale: Locale) {
    val currentSelectedLanguage = languageService.getSelectedLanguage()
    logcat { "LanguageAndMarketLaunchCheckUseCase: currentLanguage: $currentSelectedLanguage" }
    val defLanguage = Language.from(defLocale.toLanguageTag())
    logcat { "AndroidLanguageLaunchCheckUseCase setting language to $defLanguage" }
    languageService.setLanguage(defLanguage)
    val currentLanguageListAfter = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: after check language: $currentLanguageListAfter" }
  }
}
