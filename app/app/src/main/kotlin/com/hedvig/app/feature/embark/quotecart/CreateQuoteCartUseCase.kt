package com.hedvig.app.feature.embark.quotecart

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.android.market.MarketManager
import com.hedvig.app.feature.offer.model.QuoteCartId
import giraffe.CreateOnboardingQuoteCartMutation
import giraffe.type.Market

class CreateQuoteCartUseCase(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
  private val marketManager: MarketManager,
) {

  private fun mutation() = CreateOnboardingQuoteCartMutation(
    languageService.getGraphQLLocale().rawValue,
    marketManager.market?.toGraphQLMarket() ?: Market.SWEDEN,
  )

  suspend fun invoke(): Either<ErrorMessage, QuoteCartId> {
    return apolloClient
      .mutation(mutation())
      .safeExecute()
      .toEither(::ErrorMessage)
      .map { QuoteCartId(it.onboardingQuoteCart_create.id) }
  }

  private fun com.hedvig.android.market.Market.toGraphQLMarket(): Market = when (this) {
    com.hedvig.android.market.Market.SE -> Market.SWEDEN
    com.hedvig.android.market.Market.NO -> Market.NORWAY
    com.hedvig.android.market.Market.DK -> Market.DENMARK
    com.hedvig.android.market.Market.FR -> Market.UNKNOWN__
  }
}
