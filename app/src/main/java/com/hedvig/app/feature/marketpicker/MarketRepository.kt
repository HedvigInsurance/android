package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.GeoQuery

class MarketRepository(
    private val apolloClient: ApolloClient,
) {

    suspend fun geo() = apolloClient
        .query(GeoQuery())
        .await()
}
