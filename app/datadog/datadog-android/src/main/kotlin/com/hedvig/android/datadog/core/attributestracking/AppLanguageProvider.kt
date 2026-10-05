package com.hedvig.android.datadog.core.attributestracking

import com.hedvig.android.core.common.di.AppScope
import com.hedvig.android.language.LanguageService
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@ContributesIntoSet(AppScope::class)
@SingleIn(AppScope::class)
@Inject
internal class AppLanguageProvider(
  private val languageService: LanguageService,
) : DatadogAttributeProvider {
  override fun provide(): Flow<Pair<String, Any?>> {
    return languageService.language.map { language -> APP_LANGUAGE_KEY to language.toBcp47Format() }
  }

  companion object {
    private const val APP_LANGUAGE_KEY = "app_language"
  }
}
