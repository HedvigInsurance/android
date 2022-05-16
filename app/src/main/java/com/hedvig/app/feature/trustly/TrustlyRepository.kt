package com.hedvig.app.feature.trustly

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.coroutines.await
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation

class TrustlyRepository(
    private val apolloClient: ApolloClient,
) {
    suspend fun startTrustlySession() = apolloClient
        .mutation(StartDirectDebitRegistrationMutation())
        .await()
}
