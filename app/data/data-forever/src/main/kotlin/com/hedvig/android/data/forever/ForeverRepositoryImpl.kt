package com.hedvig.android.data.forever

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.cache.normalized.FetchPolicy
import com.apollographql.apollo3.cache.normalized.fetchPolicy
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.data.forever.ForeverRepository.ReferralError
import com.hedvig.android.language.LanguageService
import giraffe.RedeemReferralCodeMutation
import octopus.MemberReferralInformationCodeUpdateMutation
import octopus.ReferralsQuery

@JvmInline
value class CampaignCode(val code: String)

internal class ForeverRepositoryImpl(
  private val apolloClientOctopus: ApolloClient,
  private val apolloClientGiraffe: ApolloClient,
  private val languageService: LanguageService,
) : ForeverRepository {
  private val referralsQuery = ReferralsQuery()

  override suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = apolloClientOctopus
    .query(referralsQuery)
    .fetchPolicy(FetchPolicy.NetworkOnly)
    .safeExecute()
    .toEither(::ErrorMessage)

  override suspend fun updateCode(newCode: String): Either<ReferralError, String> = either {
    val result = apolloClientOctopus
      .mutation(MemberReferralInformationCodeUpdateMutation(newCode))
      .safeExecute()
      .toEither { message, _ -> toReferralError(message) }
      .bind()

    val error = result.memberReferralInformationCodeUpdate.userError
    val referralInformation = result.memberReferralInformationCodeUpdate.referralInformation

    if (referralInformation != null) {
      referralInformation.code
    } else if (error != null) {
      raise(ReferralError.GeneralError(error.message))
    } else {
      raise(ReferralError.GeneralError("Unknown error"))
    }
  }

  // TODO Move to payments module
  override suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
    return apolloClientGiraffe
      .mutation(RedeemReferralCodeMutation(campaignCode.code, languageService.getGraphQLLocale()))
      .safeExecute()
      .toEither(::ErrorMessage)
  }

  private fun toReferralError(message: String?) = ReferralError.GeneralError(message)
}
