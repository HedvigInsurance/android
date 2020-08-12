package com.hedvig.app.feature.home.data

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.ApolloClientWrapper

class HomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    private val homeQuery = HomeQuery()

    fun home() = apolloClientWrapper
        .apolloClient
        .query(homeQuery)
        .watcher()
        .toFlow()

    fun reloadHomeAsync() = apolloClientWrapper
        .apolloClient
        .query(homeQuery)
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .toDeferred()
}
