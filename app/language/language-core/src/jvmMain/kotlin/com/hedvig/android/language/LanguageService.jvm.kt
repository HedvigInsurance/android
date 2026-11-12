package com.hedvig.android.language

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.core.locale.CommonLocale
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
@Inject
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
