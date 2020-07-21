package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.coroutines.toDeferred
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.ApolloClientWrapper

class RedeemReferralCodeRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    fun redeemReferralCodeAsync(code: String) = apolloClientWrapper
        .apolloClient
        .mutate(RedeemReferralCodeMutation(code))
        .toDeferred()
}
