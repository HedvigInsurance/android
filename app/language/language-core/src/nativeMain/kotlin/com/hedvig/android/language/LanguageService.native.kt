package com.hedvig.android.language

import com.hedvig.android.core.locale.CommonLocale

// todo ios
internal class NativeLanguageService : LanguageService {
  override fun setLanguage(language: Language) {

  }

  override fun getSelectedLanguage(): Language? {
    return Language.EN_SE
  }

  override fun getLanguage(): Language {
    return Language.EN_SE
  }

  override fun getLocale(): CommonLocale {
    return CommonLocale(Language.EN_SE.toBcp47Format())
  }
}
