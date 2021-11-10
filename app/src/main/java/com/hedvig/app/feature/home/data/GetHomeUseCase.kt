package com.hedvig.app.feature.home.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.ApolloQueryCall
import com.apollographql.apollo.api.cache.http.HttpCachePolicy
import com.apollographql.apollo.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetHomeUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    suspend operator fun invoke(forceReload: Boolean): HomeResult {
        return when (val response = createHomeCall(forceReload = forceReload).safeQuery()) {
            is QueryResult.Error -> HomeResult.Error(response.message)
            is QueryResult.Success -> HomeResult.Home(response.data)
        }
    }

    private fun createHomeCall(forceReload: Boolean): ApolloQueryCall<HomeQuery.Data> {
        val builder = apolloClient
            .query(homeQuery())
            .toBuilder()

        if (forceReload) {
            builder
                .httpCachePolicy(HttpCachePolicy.NETWORK_ONLY)
                .responseFetcher(ApolloResponseFetchers.NETWORK_ONLY)
        }
        return builder.build()
    }

    private fun homeQuery() = HomeQuery(localeManager.defaultLocale(), localeManager.defaultLocale().rawValue)

    sealed class HomeResult {
        data class Home(val home: HomeQuery.Data) : HomeResult()
        data class Error(val message: String?) : HomeResult()
    }
}
