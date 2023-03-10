package com.hedvig.app.feature.referrals.data

import arrow.core.Either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.graphql.RedeemReferralCodeMutation
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.offer.usecase.CampaignCode

class RedeemReferralCodeRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
    return apolloClient
      .mutation(RedeemReferralCodeMutation(campaignCode.code, languageService.getGraphQLLocale()))
      .safeExecute()
      .toEither()
      .mapLeft { ErrorMessage(it.message) }
  }
}
