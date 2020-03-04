package com.hedvig.app.feature.chat.data

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.android.owldroid.graphql.NewSessionMutation
import com.hedvig.android.owldroid.graphql.SwedishBankIdAuthMutation
import com.hedvig.app.ApolloClientWrapper

class UserRepository(private val apolloClientWrapper: ApolloClientWrapper) {

    fun newUserSession() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(NewSessionMutation()))

    fun fetchAutoStartToken() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(SwedishBankIdAuthMutation()))

    fun subscribeAuthStatus() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription()))

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
