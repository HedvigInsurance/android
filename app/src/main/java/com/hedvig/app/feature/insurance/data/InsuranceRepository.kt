package com.hedvig.app.feature.insurance.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.util.LocaleManager

class InsuranceRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun insurance() = apolloClient
        .query(InsuranceQuery(localeManager.defaultLocale()))
        .await()
}
