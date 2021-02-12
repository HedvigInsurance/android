package com.hedvig.app.feature.trustly

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation

class TrustlyRepository(
    private val apolloClient: ApolloClient,
) {
    suspend fun startTrustlySession() = apolloClient
        .mutate(StartDirectDebitRegistrationMutation())
        .await()
}
