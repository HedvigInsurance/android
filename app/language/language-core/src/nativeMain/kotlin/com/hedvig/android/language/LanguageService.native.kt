package com.hedvig.android.language

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.locale.CommonLocale
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import platform.Foundation.NSLocale

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativeLanguageService(
  private val storage: LanguageStorage,
) : LanguageService {
  override fun setLanguage(language: Language) {
    storage.setLanguageTag(language.toBcp47Format())
  }

  override fun getSelectedLanguage(): Language? {
    return storage.getSelectedLanguageTag()?.let(Language::from)
  }

  override fun getLanguage(): Language {
    val languageTag = storage.getCurrentLanguageTag()
    return Language.from(languageTag)
  }

  override fun getLocale(): CommonLocale {
    return NSLocale(localeIdentifier = storage.getCurrentLanguageTag())
  }
}
