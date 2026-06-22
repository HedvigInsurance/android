package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.data.paying.member.MemberType
import kotlinx.datetime.LocalDate

data class PaymentOverview(
  val memberChargeShortInfo: MemberChargeShortInfo?,
  val ongoingCharges: List<OngoingCharge>,
  val paymentConnection: PaymentConnection,
  val isManualChargeAllowed: ManualChargeToPrompt?,
  val memberType: MemberType
) {
  data class OngoingCharge(
    val id: String,
    val date: LocalDate,
    val netAmount: UiMoney,
  )
}

data class ManualChargeToPrompt(
  val sum: UiMoney,
)

data class MemberChargeShortInfo(
  val netAmount: UiMoney,
  val dueDate: LocalDate,
  val id: String?,
  val status: MemberCharge.MemberChargeStatus,
  val failedCharge: MemberCharge.FailedCharge?,
)

enum class MemberPaymentChargeMethod {
  TRUSTLY,
  INVOICE,
  UNKNOWN,
}
