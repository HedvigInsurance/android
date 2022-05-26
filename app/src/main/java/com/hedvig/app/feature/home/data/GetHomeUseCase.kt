package com.hedvig.app.feature.home.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class GetHomeUseCase(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {

    suspend operator fun invoke(forceReload: Boolean): Either<QueryResult.Error, HomeQuery.Data> {
        val apolloCall = apolloClient.query(homeQuery())
        if (forceReload) {
            apolloCall.fetchPolicy(FetchPolicy.NetworkOnly)
        }
        return apolloCall
            .safeQuery()
            .toEither()
    }

    private fun homeQuery() = HomeQuery(localeManager.defaultLocale(), localeManager.defaultLocale().rawValue)

    data class Error(val message: String?)
}
