package com.hedvig.app.feature.marketing.data

import android.content.Context
import arrow.core.Either
import com.apollographql.apollo.ApolloClient
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery
import com.hedvig.hanalytics.HAnalytics

class GetInitialMarketPickerValuesUseCase(
    private val marketManager: MarketManager,
    private val context: Context,
    private val apolloClient: ApolloClient,
    private val hAnalytics: HAnalytics,
) {
    suspend operator fun invoke(): Either<QueryResult.Error, Pair<Market?, Language?>> {
        val currentMarket = marketManager.market
        if (currentMarket != null) {
            val currentLanguage = Language.fromSettings(context, currentMarket)
            return Either.Right(currentMarket to currentLanguage)
        }

        return apolloClient
            .query(GeoQuery())
            .safeQuery()
            .toEither()
            .map { runCatching { Market.valueOf(it.geo.countryISOCode) }.getOrNull() }
            .map { market ->
                if (market == Market.FR && !hAnalytics.frenchMarket()) {
                    null to null
                } else {
                    market to null
                }
            }
    }
}
