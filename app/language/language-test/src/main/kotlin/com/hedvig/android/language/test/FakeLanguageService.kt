package com.hedvig.android.language.test

import com.hedvig.android.language.Language
import com.hedvig.android.language.LanguageService
import java.util.Locale

@Suppress("unused")
class FakeLanguageService : LanguageService {
  override fun setLanguage(language: Language) {
    TODO("Not yet implemented")
  }

  override fun getLanguage(): Language {
    TODO("Not yet implemented")
  }

  override fun getLocale(): Locale {
    return Locale.ENGLISH
  }

  override fun getGraphQLLocale(): giraffe.type.Locale {
    return giraffe.type.Locale.en_SE
  }

  override fun performOnLaunchLanguageCheck() {
    TODO("Not yet implemented")
  }
}
