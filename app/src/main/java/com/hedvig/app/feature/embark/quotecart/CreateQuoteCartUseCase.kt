package com.hedvig.app.feature.embark.quotecart

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.market.MarketManager
import com.hedvig.android.owldroid.graphql.CreateOnboardingQuoteCartMutation
import com.hedvig.android.owldroid.graphql.type.Market
import com.hedvig.app.feature.offer.model.QuoteCartId
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.app.util.apollo.toEither

class CreateQuoteCartUseCase(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
  private val marketManager: MarketManager,
) {

  private fun mutation() = CreateOnboardingQuoteCartMutation(
    localeManager.defaultLocale().toString(),
    marketManager.market?.toGraphQLMarket() ?: Market.SWEDEN,
  )

  suspend fun invoke(): Either<ErrorMessage, QuoteCartId> {
    return apolloClient
      .mutation(mutation())
      .safeQuery()
      .toEither { ErrorMessage(it) }
      .map { QuoteCartId(it.onboardingQuoteCart_create.id) }
  }

  private fun com.hedvig.android.market.Market.toGraphQLMarket(): Market = when (this) {
    com.hedvig.android.market.Market.SE -> Market.SWEDEN
    com.hedvig.android.market.Market.NO -> Market.NORWAY
    com.hedvig.android.market.Market.DK -> Market.DENMARK
    com.hedvig.android.market.Market.FR -> Market.UNKNOWN__
  }
}
