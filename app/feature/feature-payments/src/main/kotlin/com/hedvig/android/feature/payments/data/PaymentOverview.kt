package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate

internal data class PaymentOverview(
  val memberChargeShortInfo: MemberChargeShortInfo?,
  val ongoingCharges: List<OngoingCharge>,
  val paymentConnection: PaymentConnection,
) {
  data class OngoingCharge(
    val id: String,
    val date: LocalDate,
    val netAmount: UiMoney,
  )
}

internal data class MemberChargeShortInfo(
  val netAmount: UiMoney,
  val dueDate: LocalDate,
  val id: String?,
  val status: MemberCharge.MemberChargeStatus,
  val failedCharge: MemberCharge.FailedCharge?,
)
