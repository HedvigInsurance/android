package com.hedvig.app.feature.loggedin.ui

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.QueryResult
import com.hedvig.app.util.apollo.safeQuery

class LoggedInRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun loggedInData(): Either<QueryResult.Error, LoggedInQuery.Data> = apolloClient
        .query(LoggedInQuery(localeManager.defaultLocale()))
        .safeQuery()
        .toEither()
}
