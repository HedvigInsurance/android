package com.hedvig.app.feature.chat

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.*
import com.hedvig.app.ApolloClientWrapper

class UserRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun newUserSession() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(NewSessionMutation()))

    fun fetchAutoStartToken() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(BankIdAuthMutation()))

    fun subscribeAuthStatus() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription.builder().build()))

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
