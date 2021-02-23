package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation

class RedeemReferralCodeRepository(
    private val apolloClient: ApolloClient,
) {
    suspend fun redeemReferralCode(code: String) = apolloClient
        .mutate(RedeemReferralCodeMutation(code))
        .await()
}
