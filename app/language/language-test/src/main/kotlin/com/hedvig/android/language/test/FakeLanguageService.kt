package com.hedvig.android.language.test

import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import java.util.Locale

class FakeLanguageService : LanguageService {
  override fun setLanguage(language: Language) {
    error("Not implemented")
  }

  override fun getSelectedLanguage(): Language? {
    error("Not implemented")
  }

  override fun getLanguage(): Language {
    error("Not implemented")
  }

  override fun getLocale(): Locale {
    error("Not implemented")
  }
}
