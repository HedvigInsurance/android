package com.hedvig.app.feature.norway

import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class NorwegianAuthenticationRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun startAuthAsync() = apolloClientWrapper
        .apolloClient
        .mutate(NorwegianBankIdAuthMutation())
        .toDeferred()

    fun authStatus() = apolloClientWrapper
        .apolloClient
        .subscribe(AuthStatusSubscription())
        .toFlow()
}
