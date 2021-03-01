package com.hedvig.app.feature.insurance.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.Locale

class InsuranceRepository(
    private val apolloClient: ApolloClient,
    private val defaultLocale: Locale
) {
    suspend fun insurance() = apolloClient
        .query(InsuranceQuery(defaultLocale))
        .await()
}
