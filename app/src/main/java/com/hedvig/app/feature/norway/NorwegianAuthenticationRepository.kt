package com.hedvig.app.feature.norway

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class NorwegianAuthenticationRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun authAsync() = apolloClientWrapper
        .apolloClient
        .mutate(NorwegianBankIdAuthMutation())
        .toDeferred()
}
