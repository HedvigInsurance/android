package com.hedvig.android.language

import com.hedvig.android.core.locale.CommonLocale
import java.util.Locale

internal class JvmLanguageService : LanguageService {
  override fun setLanguage(language: Language) {
    Locale.setDefault(Locale.forLanguageTag(language.toBcp47Format()))
  }

  override fun getSelectedLanguage(): Language? {
    return null
  }

  override fun getLanguage(): Language {
    return Language.from(getLocale().toLanguageTag())
  }

  override fun getLocale(): CommonLocale {
    return Locale.getDefault()
  }
}
