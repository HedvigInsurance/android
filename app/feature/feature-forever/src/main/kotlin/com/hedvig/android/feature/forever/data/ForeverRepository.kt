package com.hedvig.android.feature.forever.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import com.hedvig.android.language.LanguageService
import giraffe.RedeemReferralCodeMutation
import giraffe.ReferralsQuery
import giraffe.UpdateReferralCampaignCodeMutation

@JvmInline
value class CampaignCode(val code: String)

class ForeverRepository(
  private val apolloClient: ApolloClient,
  private val languageService: LanguageService,
) {
  private val referralsQuery = ReferralsQuery()

  suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = apolloClient
    .query(referralsQuery) // TODO include terms in this query and remove referralsTermsUseCase
    .safeExecute()
    .toEither(::ErrorMessage)

  suspend fun updateCode(newCode: String): Either<ReferralError, String> = either {
    val result = apolloClient
      .mutation(UpdateReferralCampaignCodeMutation(newCode))
      .safeExecute()
      .toEither { message, _ ->
        toReferralError(message)
      }
      .bind()

    when {
      result.updateReferralCampaignCode.asSuccessfullyUpdatedCode != null -> result.updateReferralCampaignCode.asSuccessfullyUpdatedCode!!.code
      result.updateReferralCampaignCode.asCodeTooLong != null -> raise(ReferralError.CodeTooLong(result.updateReferralCampaignCode.asCodeTooLong!!.maxCharacters))
      result.updateReferralCampaignCode.asCodeTooShort != null -> raise(ReferralError.CodeTooShort(result.updateReferralCampaignCode.asCodeTooShort!!.minCharacters))
      result.updateReferralCampaignCode.asCodeAlreadyTaken != null -> raise(ReferralError.CodeExists)
      result.updateReferralCampaignCode.asExceededMaximumUpdates != null -> raise(ReferralError.MaxUpdates(result.updateReferralCampaignCode.asExceededMaximumUpdates!!.maximumNumberOfUpdates))
      else -> raise(ReferralError.GeneralError("Unknown error"))
    }
  }

  suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> {
    return apolloClient
      .mutation(RedeemReferralCodeMutation(campaignCode.code, languageService.getGraphQLLocale()))
      .safeExecute()
      .toEither(::ErrorMessage)
  }

  private fun toReferralError(message: String?) = ReferralError.GeneralError(message)

  sealed interface ReferralError {
    data class GeneralError(
      val message: String?,
    ) : ReferralError

    data class CodeTooLong(
      val maxCharacters: Int,
    ) : ReferralError

    data class CodeTooShort(
      val minCharacters: Int,
    ) : ReferralError

    data class MaxUpdates(
      val maxUpdates: Int,
    ) : ReferralError

    data object CodeExists : ReferralError
  }
}
