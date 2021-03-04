package com.hedvig.app.feature.welcome

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.android.owldroid.type.Locale

class WelcomeRepository(
    private val apolloClient: ApolloClient,
    private val defaultLocale: Locale
) {
    suspend fun fetchWelcomeScreens() = apolloClient
        .query(WelcomeQuery(defaultLocale))
        .await()
}
