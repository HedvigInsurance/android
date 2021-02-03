package com.hedvig.app.feature.insurance.data

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.Locale
import com.hedvig.app.ApolloClientWrapper

class InsuranceRepository(
    private val apolloClientWrapper: ApolloClientWrapper,
    private val defaultLocale: Locale
) {
    suspend fun insurance() = apolloClientWrapper
        .apolloClient
        .query(InsuranceQuery(defaultLocale))
        .await()
}
