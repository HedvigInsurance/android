package com.hedvig.android.language.test

import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
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
    TODO("Not yet implemented")
  }

  override fun performOnLaunchLanguageCheck() {
    TODO("Not yet implemented")
  }
}
