package com.hedvig.app.feature.norway

import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.extensions.toDeferred
import com.hedvig.app.util.extensions.toFlow

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
