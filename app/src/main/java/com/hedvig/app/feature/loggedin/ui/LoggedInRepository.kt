package com.hedvig.app.feature.loggedin.ui

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.type.Locale

class LoggedInRepository(
    private val apolloClient: ApolloClient,
    private val defaultLocale: Locale
) {
    suspend fun loggedInData() = apolloClient
        .query(LoggedInQuery(defaultLocale))
        .await()
}
