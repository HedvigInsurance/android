package com.hedvig.app.feature.home.data

import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toLocaleString

class HomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    defaultLocale: Locale
) {
    private val homeQuery = HomeQuery(defaultLocale, defaultLocale.toLocaleString())

    fun home() = apolloClientWrapper
        .apolloClient
        .query(homeQuery)
        .watcher()
        .toFlow()

    suspend fun reloadHome() = apolloClientWrapper
        .apolloClient
        .query(homeQuery)
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()
}
