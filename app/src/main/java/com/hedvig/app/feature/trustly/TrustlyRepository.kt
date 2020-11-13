package com.hedvig.app.feature.trustly

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation
import com.hedvig.app.ApolloClientWrapper

class TrustlyRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    suspend fun startTrustlySession() = apolloClientWrapper
        .apolloClient
        .mutate(StartDirectDebitRegistrationMutation())
        .await()
}
