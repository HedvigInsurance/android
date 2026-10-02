package com.hedvig.android.language

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.locale.CommonLocale
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSLocale

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativeLanguageService(
  private val storage: LanguageStorage,
) : LanguageService {
  private val mutableLanguage = MutableStateFlow(Language.from(storage.getCurrentLanguageTag()))
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  override fun setLanguage(language: Language) {
    storage.setLanguageTag(language.toBcp47Format())
    mutableLanguage.value = language
  }

  /**
   * Read from the storage on every call, since the iOS app changes the language through its own layer without going
   * through [setLanguage].
   */
  override fun getLanguage(): Language {
    val languageTag = storage.getCurrentLanguageTag()
    return Language.from(languageTag).also { mutableLanguage.value = it }
  }

  override fun getLocale(): CommonLocale {
    return NSLocale(localeIdentifier = storage.getCurrentLanguageTag())
  }
}
