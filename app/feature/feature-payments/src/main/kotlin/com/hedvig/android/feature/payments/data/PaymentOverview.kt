package com.hedvig.android.feature.payments.data

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentOverview(
  val memberCharge: MemberCharge?,
  val pastCharges: List<MemberCharge>?,
  val discounts: List<Discount>,
  val paymentConnection: PaymentConnection?,
) {
  fun getNextCharge(selectedMemberCharge: MemberCharge): MemberCharge? {
    val index = (pastCharges?.indexOf(selectedMemberCharge) ?: 0) + 1
    return if (pastCharges != null && index > pastCharges.size - 1) {
      memberCharge
    } else {
      pastCharges?.get(index)
    }
  }
}
