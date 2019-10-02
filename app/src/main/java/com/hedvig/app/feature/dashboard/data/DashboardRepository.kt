package com.hedvig.app.feature.dashboard.data

import com.apollographql.apollo.api.Response
import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.DashboardQuery
import com.hedvig.app.ApolloClientWrapper
import io.reactivex.Observable

class DashboardRepository(
    val apolloClientWrapper: ApolloClientWrapper
) {
    fun fetchDashboard(): Observable<Response<DashboardQuery.Data>> {
        val dashboardQuery = DashboardQuery.builder().build()

        return Rx2Apollo.from(apolloClientWrapper.apolloClient.query(dashboardQuery))
    }
}
