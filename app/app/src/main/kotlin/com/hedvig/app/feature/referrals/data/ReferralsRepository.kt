package com.hedvig.app.feature.referrals.data

import arrow.core.Either
import arrow.core.raise.either
import com.apollographql.apollo3.ApolloClient
import com.hedvig.android.apollo.safeExecute
import com.hedvig.android.apollo.toEither
import com.hedvig.android.core.common.ErrorMessage
import giraffe.ReferralsQuery
import giraffe.UpdateReferralCampaignCodeMutation

class ReferralsRepository(
  private val apolloClient: ApolloClient,
) {
  private val referralsQuery = ReferralsQuery()

  suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = apolloClient
    .query(referralsQuery)
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

    object CodeExists : ReferralError
  }
}
