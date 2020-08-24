package com.hedvig.app.feature.insurance.data

import android.content.Context
import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.defaultLocale

class InsuranceRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val context: Context
) {
    fun dashboardAsync() = apolloClientWrapper
        .apolloClient
        .query(InsuranceQuery(defaultLocale(context), defaultLocale(context).rawValue))
        .toDeferred()
}
