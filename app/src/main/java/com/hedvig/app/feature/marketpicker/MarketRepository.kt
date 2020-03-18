package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.ApolloClientWrapper

class MarketRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun geoAsync() = apolloClientWrapper
        .apolloClient
        .query(GeoQuery())
        .toDeferred()
}

