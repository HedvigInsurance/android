package com.hedvig.android.language

import androidx.annotation.MainThread
import com.hedvig.android.core.locale.CommonLocale
import kotlinx.coroutines.flow.StateFlow

interface LanguageService {
  /**
   * The language the app is shown in, see [resolveLanguage].
   */
  val language: StateFlow<Language>

  /**
   * Picking the language the phone already resolves to clears the stored choice, see [storedLocaleTagsForPick].
   */
  @MainThread
  fun setLanguage(language: Language)

  fun getLanguage(): Language = language.value

  fun getLocale(): CommonLocale
}
