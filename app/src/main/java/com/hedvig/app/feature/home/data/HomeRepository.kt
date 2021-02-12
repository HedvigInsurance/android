package com.hedvig.app.feature.home.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toLocaleString

class HomeRepository(
    private val apolloClient: ApolloClient,
    context: Context,
) {
    private val defaultLocale = defaultLocale(context)
    private val homeQuery = HomeQuery(defaultLocale, defaultLocale.toLocaleString())

    fun home() = apolloClient
        .query(homeQuery)
        .watcher()
        .toFlow()

    suspend fun reloadHome() = apolloClient
        .query(homeQuery)
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()
}
