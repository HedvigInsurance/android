package com.hedvig.app.feature.chat.data

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription
import com.hedvig.android.owldroid.graphql.BankIdAuthMutation
import com.hedvig.android.owldroid.graphql.LogoutMutation
import com.hedvig.app.ApolloClientWrapper

class UserRepository(private val apolloClientWrapper: ApolloClientWrapper) {
    fun fetchAutoStartToken() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(BankIdAuthMutation()))

    fun subscribeAuthStatus() =
        Rx2Apollo.from(apolloClientWrapper.apolloClient.subscribe(AuthStatusSubscription()))

    fun logout() = Rx2Apollo.from(apolloClientWrapper.apolloClient.mutate(LogoutMutation()))
}
