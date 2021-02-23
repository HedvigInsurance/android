package com.hedvig.app.feature.welcome

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.util.apollo.defaultLocale

class WelcomeRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {
    suspend fun fetchWelcomeScreens() = apolloClient
        .query(WelcomeQuery(defaultLocale(context)))
        .await()
}
