package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.ApolloClientWrapper

class RedeemReferralCodeRepository(
    private val apolloClientWrapper: ApolloClientWrapper
) {
    suspend fun redeemReferralCode(code: String) = apolloClientWrapper
        .apolloClient
        .mutate(RedeemReferralCodeMutation(code))
        .await()
}
