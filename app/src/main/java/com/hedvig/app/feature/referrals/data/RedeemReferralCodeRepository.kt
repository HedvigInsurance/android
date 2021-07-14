package com.hedvig.app.feature.referrals.data

import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.coroutines.await
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.util.LocaleManager

class RedeemReferralCodeRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager
) {
    suspend fun redeemReferralCode(code: String) = apolloClient
        .mutate(RedeemReferralCodeMutation(code, localeManager.defaultLocale()))
        .await()
}
