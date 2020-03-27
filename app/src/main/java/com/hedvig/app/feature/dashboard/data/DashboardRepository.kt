package com.hedvig.app.feature.dashboard.data

import android.content.Context
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class DashboardRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun dashboardAsync() = apolloClientWrapper
        .apolloClient
        .query(DashboardQuery(defaultLocale(context)))
        .toDeferred()
}
