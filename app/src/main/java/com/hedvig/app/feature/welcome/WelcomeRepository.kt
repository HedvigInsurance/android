package com.hedvig.app.feature.welcome

import android.content.Context
import com.apollographql.apollo.api.Response
import com.hedvig.android.owldroid.graphql.WelcomeQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toDeferred
import kotlinx.coroutines.Deferred

class WelcomeRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun fetchWelcomeScreensAsync(): Deferred<Response<WelcomeQuery.Data>> {
        val welcomeQuery = WelcomeQuery(
            locale = defaultLocale(context)
        )

        return apolloClientWrapper.apolloClient.query(welcomeQuery).toDeferred()
    }
}
