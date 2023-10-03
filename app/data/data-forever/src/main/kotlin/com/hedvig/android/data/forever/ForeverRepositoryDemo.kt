package com.hedvig.android.data.forever

import arrow.core.Either
import arrow.core.raise.either
import com.hedvig.android.core.common.ErrorMessage
import giraffe.RedeemReferralCodeMutation
import giraffe.ReferralsQuery

internal class ForeverRepositoryDemo : ForeverRepository {
  override suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data> = either {
    ReferralsQuery.Data()
  }

  override suspend fun updateCode(newCode: String): Either<ForeverRepository.ReferralError, String> = either {
    newCode
  }

  override suspend fun redeemReferralCode(
    campaignCode: CampaignCode,
  ): Either<ErrorMessage, RedeemReferralCodeMutation.Data?> = either {
    null
  }
}
