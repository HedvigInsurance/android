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
    private val localeManager: LocaleManager,
    private val marketManager: MarketManager,
) {

    sealed interface Error {
        object NetworkError : Error
    }

    @JvmInline
    value class QuoteCartId(val id: String)

    suspend operator fun invoke(): Either<Error, QuoteCartId> {
        return apolloClient
            .mutate(
                CreateOnboardingQuoteCartMutation(
                    localeManager.defaultLocale().toString(),
                    marketManager.market!!.toGraphQLMarket()
                )
            )
            .safeQuery()
            .toEither {
                Error.NetworkError
            }
            .map { data ->
                QuoteCartId(data.onboardingQuoteCart_create.id)
            }
    }

    private fun com.hedvig.app.feature.settings.Market.toGraphQLMarket(): Market = when (this) {
        com.hedvig.app.feature.settings.Market.SE -> Market.SWEDEN
        com.hedvig.app.feature.settings.Market.NO -> Market.NORWAY
        com.hedvig.app.feature.settings.Market.DK -> Market.DENMARK
        com.hedvig.app.feature.settings.Market.FR -> Market.UNKNOWN__
    }
}
