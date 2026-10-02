package com.hedvig.android.language

import com.hedvig.android.core.locale.CommonLocale
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class JvmLanguageService : LanguageService {
  private val mutableLanguage = MutableStateFlow(Language.from(Locale.getDefault().toLanguageTag()))
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  override fun setLanguage(language: Language) {
    Locale.setDefault(Locale.forLanguageTag(language.toBcp47Format()))
    mutableLanguage.value = language
  }

  override fun getLocale(): CommonLocale {
    return Locale.getDefault()
  }
}
