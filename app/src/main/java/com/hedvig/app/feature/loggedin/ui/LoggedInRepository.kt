package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class LoggedInRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    suspend fun loggedInData() = apolloClientWrapper
        .apolloClient
        .query(LoggedInQuery(defaultLocale(context)))
        .await()
}
