package com.hedvig.app.feature.trustly

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.ApolloResponse
import giraffe.StartDirectDebitRegistrationMutation

class TrustlyRepository(
  private val apolloClient: ApolloClient,
) {
  suspend fun startTrustlySession(): ApolloResponse<StartDirectDebitRegistrationMutation.Data> = apolloClient
    .mutation(StartDirectDebitRegistrationMutation())
    .execute()
}
