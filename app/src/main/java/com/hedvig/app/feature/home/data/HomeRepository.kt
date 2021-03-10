package com.hedvig.app.feature.home.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.LocaleManager

class HomeRepository(
    private val apolloClient: ApolloClient,
    localeManager: LocaleManager
) {
    private val homeQuery = HomeQuery(localeManager.defaultLocale(), localeManager.defaultLocale().rawValue)

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
