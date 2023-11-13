package com.hedvig.android.feature.forever.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import octopus.ReferralsQuery

internal interface ForeverRepository {
  suspend fun getReferralsData(): Either<ErrorMessage, ReferralsQuery.Data>

  suspend fun updateCode(newCode: String): Either<ReferralError, String>

  data class ReferralError(val message: String?)
}
