package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.ApolloClientWrapper

class MarketRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    suspend fun geo() = apolloClientWrapper
        .apolloClient
        .query(GeoQuery())
        .await()
}
