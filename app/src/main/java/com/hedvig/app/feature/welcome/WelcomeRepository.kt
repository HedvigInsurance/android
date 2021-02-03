package com.hedvig.app.feature.welcome

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val defaultLocale: Locale
) {
    suspend fun fetchWelcomeScreens() = apolloClientWrapper
        .apolloClient
        .query(WelcomeQuery(defaultLocale))
        .await()
}
