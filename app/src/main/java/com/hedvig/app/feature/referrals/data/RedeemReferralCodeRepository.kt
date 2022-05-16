package com.hedvig.app.feature.referrals.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.offer.usecase.CampaignCode
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.app.util.apollo.safeQuery

class RedeemReferralCodeRepository(
    private val apolloClient: ApolloClient,
    private val localeManager: LocaleManager,
) {
    suspend fun redeemReferralCode(
        campaignCode: CampaignCode
    ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
        return apolloClient
            .mutate(RedeemReferralCodeMutation(campaignCode.code, localeManager.defaultLocale()))
            .safeQuery()
            .toEither()
            .mapLeft { ErrorMessage(it.message) }
    }
}
