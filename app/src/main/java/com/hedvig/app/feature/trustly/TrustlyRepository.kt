package com.hedvig.app.feature.trustly

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.StartDirectDebitRegistrationMutation
import com.hedvig.app.ApolloClientWrapper

class TrustlyRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun startTrustlySessionAsync() = apolloClientWrapper
        .apolloClient
        .mutate(StartDirectDebitRegistrationMutation())
        .toDeferred()
}
