package com.hedvig.app.feature.zignsec

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.android.owldroid.graphql.NorwegianBankIdAuthMutation

class ZignSecAuthRepository(
    private val apolloClient: ApolloClient,
) {
    // TODO: Delete this entire file
    suspend fun startDanishAuth() = apolloClient
        .mutate(DanishAuthMutation("asd"))
        .await()

    suspend fun startNorwegianAuth() = apolloClient
        .mutate(NorwegianBankIdAuthMutation("asd"))
        .await()

    fun authStatus() = apolloClient
        .subscribe(AuthStatusSubscription())
        .toFlow()
}
