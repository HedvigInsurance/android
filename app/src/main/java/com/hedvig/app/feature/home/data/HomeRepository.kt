package com.hedvig.app.feature.home.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.QuoteBundleInput
import com.hedvig.app.feature.offer.OfferPersistenceManager
import com.hedvig.app.util.LocaleManager
import kotlinx.coroutines.flow.map

class HomeRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
    private val persistenceManager: OfferPersistenceManager
) {

    sealed class HomeResult {
        data class Error(val message: String?) : HomeResult()
        data class Home(val data: HomeQuery.Data) : HomeResult()
    }

    fun home() = apolloClient
        .query(homeQuery())
        .watcher()
        .toFlow()
        .map(::toResult)

    private fun toResult(response: Response<HomeQuery.Data>) = when {
        response.data != null -> HomeResult.Home(response.data!!)
        response.errors?.isNotEmpty() == true -> {
            HomeResult.Error(response.errors?.joinToString())
        }
        else -> HomeResult.Error(null)
    }

    suspend fun reloadHome() = apolloClient
        .query(homeQuery())
        .toBuilder()
        .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
        .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        .build()
        .await()

    private fun homeQuery() = HomeQuery(
        locale = localeManager.defaultLocale(),
        languageCode = localeManager.defaultLocale().rawValue,
        quoteBundleInput = QuoteBundleInput(persistenceManager.getPersistedQuoteIds().toList())
    )
}
