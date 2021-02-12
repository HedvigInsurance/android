package com.hedvig.app.feature.insurance.data

import android.content.Context
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.apollo.defaultLocale

class InsuranceRepository(
    private val apolloClient: ApolloClient,
    private val context: Context,
) {
    suspend fun insurance() = apolloClient
        .query(InsuranceQuery(defaultLocale(context)))
        .await()
}
