package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.HedvigApplication
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.shouldOverrideFeatureFlags

class MarketRepository(
    private val apolloClient: ApolloClient,
    private val marketManager: MarketManager,
    private val application: HedvigApplication
) {

    suspend fun getMarket(): Market {
        return marketManager.market ?: getRemoteMarket() ?: Market.SE
    }

    private suspend fun getRemoteMarket(): Market? {
        return fetchGeo().data?.toMarket()
    }

    private suspend fun fetchGeo() = apolloClient
        .query(GeoQuery())
        .await()

    private fun GeoQuery.Data.toMarket(): Market {
        return try {
            Market.valueOf(
                if (geo.countryISOCode == "DK") {
                    if (shouldOverrideFeatureFlags(application)) {
                        geo.countryISOCode
                    } else {
                        Market.SE.name
                    }
                } else {
                    geo.countryISOCode
                }
            )
        } catch (e: IllegalArgumentException) {
            return Market.SE
        }
    }
}

fun Market?.toLanguage() = when (this) {
    Market.SE -> Language.EN_SE
    Market.NO -> Language.EN_NO
    Market.DK -> Language.EN_DK
    else -> Language.EN_SE
}
