package com.hedvig.app.feature.home.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.ApolloQueryCall
import com.apollographql.apollo3.api.cache.http.HttpCachePolicy
import com.apollographql.apollo3.fetcher.ApolloResponseFetchers
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetHomeUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    suspend operator fun invoke(forceReload: Boolean): Either<QueryResult.Error, HomeQuery.Data> {
        return createHomeCall(forceReload = forceReload)
            .safeQuery()
            .toEither()
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

    data class Error(val message: String?)
}
