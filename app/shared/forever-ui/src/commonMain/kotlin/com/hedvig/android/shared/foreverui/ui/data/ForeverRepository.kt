package com.hedvig.android.shared.foreverui.ui.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage
import octopus.FullReferralsQuery

interface ForeverRepository {
  suspend fun getReferralsData(): Either<ErrorMessage, FullReferralsQuery.Data>

  suspend fun updateCode(newCode: String): Either<ReferralError, String>

  data class ReferralError(val message: String?)
}
