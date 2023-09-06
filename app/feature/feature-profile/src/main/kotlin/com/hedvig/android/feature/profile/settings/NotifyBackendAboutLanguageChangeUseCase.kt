package com.hedvig.android.feature.profile.settings

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.NetworkCacheManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.market.Language
import giraffe.UpdateLanguageMutation

internal interface NotifyBackendAboutLanguageChangeUseCase {
  suspend fun invoke(language: Language)
}

internal class NotifyBackendAboutLanguageChangeUseCaseImpl(
  private val apolloClient: ApolloClient,
  private val cacheManager: NetworkCacheManager,
) : NotifyBackendAboutLanguageChangeUseCase {
  override suspend fun invoke(language: Language) {
    apolloClient.mutation(UpdateLanguageMutation(language.toString(), language.toLocale())).safeExecute()
    cacheManager.clearCache()
  }
}
