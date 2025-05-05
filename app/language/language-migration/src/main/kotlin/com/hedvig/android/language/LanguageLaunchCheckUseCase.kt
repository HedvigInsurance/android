package com.hedvig.android.language

import androidx.appcompat.app.AppCompatDelegate
import com.hedvig.android.logger.logcat
import java.util.Locale

/**
 * Runs once on app startup to ensure that the language from the current market is properly set.
 * https://developer.android.com/guide/topics/resources/app-languages#impl-overview
 */
interface LanguageLaunchCheckUseCase {
  suspend fun invoke(defLocale: Locale)
}

internal class AndroidLanguageLaunchCheckUseCase(
  private val languageService: LanguageService,
) : LanguageLaunchCheckUseCase {
  override suspend fun invoke(defLocale: Locale) {
    val currentSelectedLanguage = languageService.getSelectedLanguage()
    logcat { "LanguageAndMarketLaunchCheckUseCase: currentLanguage: $currentSelectedLanguage" }
    if (currentSelectedLanguage == null) {
      val defLanguage = Language.from(defLocale.toLanguageTag())
      languageService.setLanguage(defLanguage).also {
        logcat { "SetMarketUseCase setting language to $it" }
      }
    }
    val currentLanguageListAfter = AppCompatDelegate.getApplicationLocales()
    logcat { "LanguageAndMarketLaunchCheckUseCase: after check language: $currentLanguageListAfter" }
  }
}
