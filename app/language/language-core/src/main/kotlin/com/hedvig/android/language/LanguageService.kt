package com.hedvig.android.language

import androidx.annotation.MainThread
import java.util.Locale

interface LanguageService {
  /**
   * Note that AppCompatDelegate.setApplicationLocales must be called at least after Activity.onCreate()
   * https://developer.android.com/guide/topics/resources/app-languages#androidx-impl
   * https://cs.android.com/androidx/platform/frameworks/support/+/336a1f14a9e8e6729e833b3022ad3ffa8a3f0433:appcompat/appcompat/src/main/java/androidx/appcompat/app/AppCompatDelegate.java;l=733-734
   */
  @MainThread
  fun setLanguage(language: Language)

  /**
   * Returns the language that was selected by the user, or null if no language was manually selected.
   */
  fun getSelectedLanguage(): Language?

  fun getLanguage(): Language

  fun getLocale(): Locale
}
