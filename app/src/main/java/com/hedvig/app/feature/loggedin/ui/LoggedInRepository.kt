package com.hedvig.app.feature.loggedin.ui

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper

class LoggedInRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val defaultLocale: Locale
) {
    suspend fun loggedInData() = apolloClientWrapper
        .apolloClient
        .query(LoggedInQuery(defaultLocale))
        .await()
}
