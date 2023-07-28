package com.hedvig.android.feature.profile.data

import com.hedvig.android.market.Market
import hedvig.resources.R
import javax.money.MonetaryAmount

data class ProfileData(
  val member: Member,
  val chargeEstimation: ChargeEstimation,
  val directDebitStatus: DirectDebitStatus?,
  val activePaymentMethods: PaymentMethod?,
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
      is PaymentMethod.CardPaymentMethod -> hedvig.resources.R.string.Card_Connected
      is PaymentMethod.ThirdPartyPaymentMethd -> hedvig.resources.R.string.Third_Party_Connected
      null -> hedvig.resources.R.string.Card_Not_Connected
    }
    Market.FR -> throw IllegalArgumentException("Can not get price caption for market")
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
  ACTIVE, PENDING, NEEDS_SETUP, NONE, UNKNOWN
}

sealed interface PaymentMethod {
  data class CardPaymentMethod(
    val brand: String?,
    val lastFourDigits: String,
    val expiryMonth: String,
    val expiryYear: String,
  ) : PaymentMethod

  data class ThirdPartyPaymentMethd(
    val name: String,
    val type: String,
  ) : PaymentMethod
}
