package com.hedvig.app.feature.travel

import com.apollographql.apollo.rx2.Rx2Apollo
import com.hedvig.android.owldroid.graphql.LuggageClaimMutation
import com.hedvig.android.owldroid.type.CreateLuggageClaimInput
import com.hedvig.app.ApolloClientWrapper

class TravelRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun createLuggageClaim(input: CreateLuggageClaimInput) = Rx2Apollo.from(
        apolloClientWrapper
            .apolloClient
            .mutate(LuggageClaimMutation(input))
    )
}
