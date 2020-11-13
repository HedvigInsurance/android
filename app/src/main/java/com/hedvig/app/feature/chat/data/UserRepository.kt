package com.hedvig.app.feature.chat.data

import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class UserRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {

    suspend fun fetchAutoStartToken() =
        apolloClientWrapper.apolloClient.mutate(SwedishBankIdAuthMutation()).await()

    fun subscribeAuthStatus() =
        apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription()).toFlow()

    suspend fun logout() =
        apolloClientWrapper.apolloClient.mutate(LogoutMutation()).await()
}
