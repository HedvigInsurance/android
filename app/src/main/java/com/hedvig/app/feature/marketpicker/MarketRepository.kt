package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.HedvigApplication
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.shouldOverrideFeatureFlags
import com.hedvig.app.util.extensions.getMarket

class MarketRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {

    suspend fun getMarket(): Market? {
        return context.getMarket() ?: getRemoteMarket()
    }

    private suspend fun getRemoteMarket(): Market? {
        return fetchGeo().data?.toMarket()
    }

    private suspend fun fetchGeo() = apolloClientWrapper
        .apolloClient
        .query(GeoQuery())
        .await()

    private fun GeoQuery.Data.toMarket() = Market.valueOf(
        if (geo.countryISOCode == "DK") {
            if (shouldOverrideFeatureFlags(context.applicationContext as HedvigApplication)) {
                geo.countryISOCode
            } else {
                Market.SE.name
            }
        } else {
            geo.countryISOCode
        }
    )
}

fun Market?.toLanguage() = when (this) {
    Market.SE -> Language.EN_SE
    Market.NO -> Language.EN_NO
    Market.DK -> Language.EN_DK
    else -> Language.EN_SE
}
