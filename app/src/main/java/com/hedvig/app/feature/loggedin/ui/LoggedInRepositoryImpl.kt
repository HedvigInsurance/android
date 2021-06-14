package com.hedvig.app.feature.loggedin.ui

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.util.LocaleManager
import javax.inject.Inject

class LoggedInRepositoryImpl @Inject constructor(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) : LoggedInRepository {
    override suspend fun loggedInData() = apolloClient
        .query(LoggedInQuery(localeManager.defaultLocale()))
        .await()
}
