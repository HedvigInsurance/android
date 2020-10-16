package com.hedvig.app.feature.zignsec

import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class ZignSecAuthRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun startDanishAuthAsync() = apolloClientWrapper
        .apolloClient
        .mutate(DanishAuthMutation())
        .toDeferred()

    fun startNorwegianAuthAsync() = apolloClientWrapper
        .apolloClient
        .mutate(NorwegianBankIdAuthMutation())
        .toDeferred()

    fun authStatus() = apolloClientWrapper
        .apolloClient
        .subscribe(AuthStatusSubscription())
        .toFlow()
}
