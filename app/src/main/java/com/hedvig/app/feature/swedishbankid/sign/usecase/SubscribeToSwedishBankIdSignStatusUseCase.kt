package com.hedvig.app.feature.swedishbankid.sign.usecase

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.toFlow
import com.hedvig.android.owldroid.graphql.SignStatusSubscription

class SubscribeToSwedishBankIdSignStatusUseCase(
    private val apolloClient: ApolloClient,
) {
    operator fun invoke() = apolloClient
        .subscribe(SignStatusSubscription())
        .toFlow()
}
