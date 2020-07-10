package com.hedvig.app.feature.welcome

import android.content.Context
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchWelcomeScreensAsync() = apolloClientWrapper
        .apolloClient
        .query(WelcomeQuery(defaultLocale(context)))
        .toDeferred()
}
