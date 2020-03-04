package com.hedvig.app.feature.marketpicker

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.GeoQuery
import com.hedvig.app.ApolloClientWrapper
import kotlinx.coroutines.flow.Flow

class MarketRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    private var geoQuery = GeoQuery()

    fun geo(): Flow<Response<GeoQuery.Data>> {
        return apolloClientWrapper
            .apolloClient
            .query(geoQuery)
            .watcher()
            .toFlow()
    }
}
