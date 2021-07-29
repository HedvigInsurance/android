package com.hedvig.app.feature.chat.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.util.apollo.safeQuery

class UserRepository(
    private val apolloClient: ApolloClient,
) {
    suspend fun fetchAutoStartToken() =
        apolloClient.mutate(SwedishBankIdAuthMutation()).await()

    fun subscribeAuthStatus() =
        apolloClient.subscribe(AuthStatusSubscription()).toFlow()

    suspend fun logout() = apolloClient.mutate(LogoutMutation()).safeQuery()
}
