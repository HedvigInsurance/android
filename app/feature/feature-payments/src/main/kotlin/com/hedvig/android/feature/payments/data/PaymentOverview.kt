package com.hedvig.android.feature.payments.data

import com.hedvig.android.core.uidata.UiMoney
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentOverview(
  val memberChargeShortInfo: MemberChargeShortInfo?,
  val paymentConnection: PaymentConnection?,
)

@Serializable
internal data class MemberChargeShortInfo(
  val netAmount: UiMoney,
  val dueDate: LocalDate,
  val id: String,
  val status: MemberCharge.MemberChargeStatus,
  val failedCharge: MemberCharge.FailedCharge?,
)
