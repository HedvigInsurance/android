package com.hedvig.app.feature.zignsec

import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class ZignSecAuthRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    suspend fun startDanishAuth() = apolloClientWrapper
        .apolloClient
        .mutate(DanishAuthMutation())
        .await()

    suspend fun startNorwegianAuth() = apolloClientWrapper
        .apolloClient
        .mutate(NorwegianBankIdAuthMutation())
        .await()

    fun authStatus() = apolloClientWrapper
        .apolloClient
        .subscribe(AuthStatusSubscription())
        .toFlow()
}
