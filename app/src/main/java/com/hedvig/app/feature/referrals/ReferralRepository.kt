package com.hedvig.app.feature.referrals

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.ApolloClientWrapper

class ReferralRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun redeemReferralCodeAsync(code: String) = apolloClientWrapper
        .apolloClient
        .mutate(RedeemReferralCodeMutation(code))
        .toDeferred()
}
