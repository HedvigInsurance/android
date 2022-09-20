package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager

class SubmitMarketAndLanguagePreferencesUseCase(
  private val apolloClient: ApolloClient,
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  suspend fun invoke() = apolloClient
    .mutation(
      UpdateLanguageMutation(
        languageService.getLocale().toLanguageTag(),
        languageService.getGraphQLLocale(),
      ),
    )
    .safeExecute()
    .toEither()
    .tap { marketManager.hasSelectedMarket = true }
}
