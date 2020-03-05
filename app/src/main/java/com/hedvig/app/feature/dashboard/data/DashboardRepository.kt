package com.hedvig.app.feature.dashboard.data

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.ApolloClientWrapper

class DashboardRepository(
    val apolloClientWrapper: ApolloClientWrapper
) {
    fun dashboardAsync() = apolloClientWrapper
        .apolloClient
        .query(DashboardQuery())
        .toDeferred()
}
