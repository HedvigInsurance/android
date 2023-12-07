package com.hedvig.android.feature.payments2.data

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentOverview(
  val memberCharge: MemberCharge?,
  val pastCharges: List<MemberCharge>?,
  val paymentConnection: PaymentConnection?,
)
