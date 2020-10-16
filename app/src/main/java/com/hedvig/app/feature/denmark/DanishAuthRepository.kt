package com.hedvig.app.feature.denmark

import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.DanishAuthMutation
import com.hedvig.app.ApolloClientWrapper

class DanishAuthRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun startDanishAuthAsync() = apolloClientWrapper
        .apolloClient
        .mutate(DanishAuthMutation())
        .toDeferred()

    fun authStatus() = apolloClientWrapper
        .apolloClient
        .subscribe(AuthStatusSubscription())
        .toFlow()
}
