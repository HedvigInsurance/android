package com.hedvig.android.data.forever

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.RedeemReferralCodeMutation
import octopus.ReferralsQuery

interface ForeverRepository {
  suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data>
  suspend fun updateCode(newCode: String): Either<ReferralError, String>
  suspend fun redeemReferralCode(campaignCode: CampaignCode): Either<ErrorMessage, RedeemReferralCodeMutation.Data?>

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

    data object CodeIsEmpty : ReferralError
    data object CodeExists : ReferralError
  }
}
