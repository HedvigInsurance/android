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

/** The iOS app's language. The iOS app owns the setting, so [setLanguage] only changes what Kotlin reports. */
@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class NativeLanguageService(
  private val source: IosLanguageSource,
) : LanguageService {
  private val mutableLanguage = MutableStateFlow(source.currentLanguage())

  /** Updated only when [setLanguage] or [getLanguage] runs, so read [getLanguage] for the current value. */
  override val language: StateFlow<Language> = mutableLanguage.asStateFlow()

  override fun setLanguage(language: Language) {
    mutableLanguage.value = language
  }

  /** Asks the iOS app on every call, since it changes the language without telling Kotlin. */
  override fun getLanguage(): Language {
    return source.currentLanguage().also { mutableLanguage.value = it }
  }

  override fun getLocale(): CommonLocale {
    return NSLocale(localeIdentifier = getLanguage().toBcp47Format())
  }
}
