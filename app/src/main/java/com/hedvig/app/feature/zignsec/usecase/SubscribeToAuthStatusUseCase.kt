package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription

class SubscribeToAuthStatusUseCase(
    private val apolloClient: ApolloClient,
) {
    operator fun invoke() = apolloClient.subscribe(AuthStatusSubscription()).toFlow()
}
