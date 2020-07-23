package com.hedvig.app.feature.chat.data

import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class UserRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun newUserSessionAsync() =
        apolloClientWrapper.apolloClient.mutate(NewSessionMutation()).toDeferred()

    fun fetchAutoStartTokenAsync() =
        apolloClientWrapper.apolloClient.mutate(SwedishBankIdAuthMutation()).toDeferred()

    fun subscribeAuthStatus() =
        apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription()).toFlow()

    fun logoutAsync() =
        apolloClientWrapper.apolloClient.mutate(LogoutMutation()).toDeferred()
}
