package com.hedvig.android.feature.profile.settings

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.Language
import giraffe.UpdateLanguageMutation

class ChangeLanguageUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val cacheManager: NetworkCacheManager,
) {
  suspend fun invoke(language: Language) {
    apolloClient
      .mutation(
        UpdateLanguageMutation(language.toString(), language.toLocale()),
      ).safeExecute()
    cacheManager.clearCache()
    languageService.setLanguage(language)
  }
}
