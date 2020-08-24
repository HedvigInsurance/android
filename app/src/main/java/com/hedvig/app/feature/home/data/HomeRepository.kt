package com.hedvig.app.feature.home.data

import android.content.Context
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toLocaleString

class HomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    context: Context
) {
    private val defaultLocale = defaultLocale(context)
    private val homeQuery = HomeQuery(defaultLocale, defaultLocale.toLocaleString())

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
