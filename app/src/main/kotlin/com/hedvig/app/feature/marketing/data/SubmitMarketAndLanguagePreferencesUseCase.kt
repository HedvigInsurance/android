package com.hedvig.app.feature.marketing.data

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.UpdateLanguageMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.market.MarketManager
import com.hedvig.app.makeLocaleString
import com.hedvig.app.util.LocaleManager

class SubmitMarketAndLanguagePreferencesUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
  private val marketManager: MarketManager,
) {
  suspend fun invoke() = apolloClient
    .mutation(
      UpdateLanguageMutation(
        makeLocaleString(),
        localeManager.defaultLocale(),
      ),
    )
    .safeExecute()
    .toEither()
    .tap { marketManager.hasSelectedMarket = true }
    .map { }
}
