package com.hedvig.app.feature.chat.data

import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.util.apollo.toDeferred
import com.hedvig.app.util.apollo.toFlow

class UserRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    suspend fun newUserSession() =
        apolloClientWrapper.apolloClient.mutate(NewSessionMutation()).toDeferred().await()

    suspend fun fetchAutoStartToken() =
        apolloClientWrapper.apolloClient.mutate(SwedishBankIdAuthMutation()).toDeferred().await()

    fun subscribeAuthStatus() =
        apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription()).toFlow()

    suspend fun logout() =
        apolloClientWrapper.apolloClient.mutate(LogoutMutation()).toDeferred().await()
}
