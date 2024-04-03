package com.hedvig.android.feature.payments.data

import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentOverview(
  val memberCharge: MemberCharge?,
  val paymentConnection: PaymentConnection?,
)
