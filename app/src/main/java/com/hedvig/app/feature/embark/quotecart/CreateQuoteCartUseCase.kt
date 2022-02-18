package com.hedvig.app.feature.embark.quotecart

import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.CreateOnboardingQuoteCartMutation
import com.hedvig.android.owldroid.type.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class CreateQuoteCartUseCase(
    val apolloClient: ApolloClient,
    localeManager: LocaleManager,
    marketManager: MarketManager,
) {

    private val mutation = CreateOnboardingQuoteCartMutation(
        localeManager.defaultLocale().toString(),
        marketManager.market!!.toGraphQLMarket()
    )

    data class Error(val message: String?)

    @JvmInline
    value class QuoteCartId(val id: String)

    suspend operator fun invoke(): Either<Error, QuoteCartId> {
        return apolloClient
            .mutate(mutation)
            .safeQuery()
            .toEither { Error(it) }
            .map { QuoteCartId(it.onboardingQuoteCart_create.id) }
    }

    private fun com.hedvig.app.feature.settings.Market.toGraphQLMarket(): Market = when (this) {
        com.hedvig.app.feature.settings.Market.SE -> Market.SWEDEN
        com.hedvig.app.feature.settings.Market.NO -> Market.NORWAY
        com.hedvig.app.feature.settings.Market.DK -> Market.DENMARK
        com.hedvig.app.feature.settings.Market.FR -> Market.UNKNOWN__
    }
}
