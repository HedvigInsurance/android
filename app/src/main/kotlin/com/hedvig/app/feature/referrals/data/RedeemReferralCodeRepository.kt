package com.hedvig.app.feature.referrals.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.offer.usecase.CampaignCode
import com.hedvig.app.util.ErrorMessage
import com.hedvig.app.util.LocaleManager
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither

class RedeemReferralCodeRepository(
  private val apolloClient: ApolloClient,
  private val localeManager: LocaleManager,
) {
  suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
    return apolloClient
      .mutation(RedeemReferralCodeMutation(campaignCode.code, localeManager.defaultLocale()))
      .safeExecute()
      .toEither()
      .mapLeft { ErrorMessage(it.message) }
  }
}
