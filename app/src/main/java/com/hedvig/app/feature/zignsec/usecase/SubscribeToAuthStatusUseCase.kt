package com.hedvig.app.feature.zignsec.usecase

import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.AuthStatusSubscription

class SubscribeToAuthStatusUseCase(
    private val apolloClient: ApolloClient,
) {
    operator fun invoke() = apolloClient.subscription(AuthStatusSubscription()).toFlow()
}
