package com.hedvig.android.feature.profile.data

import com.hedvig.android.market.Market
import com.hedvig.android.payment.PaymentData
import hedvig.resources.R
import javax.money.MonetaryAmount

internal data class ProfileData(
  val member: Member,
  val chargeEstimation: ChargeEstimation,
  val directDebitStatus: DirectDebitStatus?,
  val activePaymentMethods: PaymentData.PaymentMethod?,
) {
  fun getPriceCaption(market: Market): Int = when (market) {
    Market.SE -> when (directDebitStatus) {
      DirectDebitStatus.ACTIVE -> R.string.Direct_Debit_Connected
      DirectDebitStatus.NEEDS_SETUP,
      DirectDebitStatus.PENDING,
      DirectDebitStatus.UNKNOWN,
      DirectDebitStatus.NONE,
      null,
      -> R.string.Direct_Debit_Not_Connected
    }
    Market.DK,
    Market.NO,
    -> when (activePaymentMethods) {
      is PaymentData.PaymentMethod.CardPaymentMethod -> hedvig.resources.R.string.Card_Connected
      is PaymentData.PaymentMethod.ThirdPartyPaymentMethd -> hedvig.resources.R.string.Third_Party_Connected
      null -> hedvig.resources.R.string.Card_Not_Connected
    }
  }
}

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
