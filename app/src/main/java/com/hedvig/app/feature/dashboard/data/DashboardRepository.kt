package com.hedvig.app.feature.dashboard.data

import android.content.Context
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale
import com.hedvig.app.util.apollo.toDeferred

class DashboardRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun dashboardAsync() = apolloClientWrapper
        .apolloClient
        .query(DashboardQuery(defaultLocale(context), defaultLocale(context).rawValue))
        .toDeferred()
}
