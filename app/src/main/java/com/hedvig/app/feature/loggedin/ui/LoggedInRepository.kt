package com.hedvig.app.feature.loggedin.ui

import android.content.Context
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toDeferred

class LoggedInRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun loggedInDataAsync() = apolloClientWrapper
        .apolloClient
        .query(LoggedInQuery(defaultLocale(context)))
        .toDeferred()
}
