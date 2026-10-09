package com.hedvig.android.language.test

import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeLanguageService(
  initialLanguage: Language = Language.EN_SE,
  private val fixedLocale: Locale? = null,
) : LanguageService {
  private val mutableLanguage = MutableStateFlow(initialLanguage)
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  val pickedLanguages = mutableListOf<Language>()

  override fun setLanguage(language: Language) {
    pickedLanguages += language
    mutableLanguage.value = language
  }

  /**
   * Simulates a change that did not come from [setLanguage], like the system per-app language settings.
   */
  fun changeLanguageExternally(language: Language) {
    mutableLanguage.value = language
  }

  override fun getLocale(): Locale {
    return fixedLocale ?: Locale.forLanguageTag(mutableLanguage.value.toBcp47Format())
  }
}
