package com.hedvig.android.feature.profile.data

import com.hedvig.android.payment.PaymentData
import javax.money.MonetaryAmount

internal data class ProfileData(
  val member: Member,
  val chargeEstimation: ChargeEstimation,
  val directDebitStatus: DirectDebitStatus?,
  val activePaymentMethods: PaymentData.PaymentMethod?,
)

data class Member(
  val id: String,
  val firstName: String,
  val lastName: String,
  val email: String,
  val phoneNumber: String?,
)

data class ChargeEstimation(
  val subscription: MonetaryAmount,
  val discount: MonetaryAmount,
  val charge: MonetaryAmount,
)

enum class DirectDebitStatus {
  ACTIVE,
  PENDING,
  NEEDS_SETUP,
  NONE,
  UNKNOWN,
}
