package com.hedvig.app.feature.welcome

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.util.LocaleManager

class WelcomeRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun fetchWelcomeScreens() = apolloClient
        .query(WelcomeQuery(localeManager.defaultLocale()))
        .await()
}
