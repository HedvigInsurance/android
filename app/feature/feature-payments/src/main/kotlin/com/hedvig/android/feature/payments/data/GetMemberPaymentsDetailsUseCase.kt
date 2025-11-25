package com.hedvig.android.feature.payments.data

import arrow.core.Either
import com.hedvig.android.core.common.ErrorMessage

internal interface GetMemberPaymentsDetailsUseCase {
  suspend fun invoke(): Either<ErrorMessage,MemberPaymentsDetails>
}

data class MemberPaymentsDetails(
  val chargingDayInTheMonth: Int?,
  val description : String?,
  val displayName : String?,
  val mandate : String?,
  val paymentMethod : String
)
