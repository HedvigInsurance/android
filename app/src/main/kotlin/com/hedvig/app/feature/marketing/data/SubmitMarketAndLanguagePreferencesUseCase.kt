package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.market.MarketManager
import com.hedvig.app.LanguageService
import com.hedvig.app.util.GraphQLLocaleService

class SubmitMarketAndLanguagePreferencesUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: GraphQLLocaleService,
  private val marketManager: MarketManager,
  private val languageService: LanguageService,
) {
  suspend fun invoke() = apolloClient
    .mutation(
      UpdateLanguageMutation(
        languageService.getLocale().toLanguageTag(),
        localeManager.defaultLocale(),
      ),
    )
    .safeExecute()
    .toEither()
    .tap { marketManager.hasSelectedMarket = true }
    .map { }
}
