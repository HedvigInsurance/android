package com.hedvig.app.feature.offer.model

import com.hedvig.app.R

enum class CheckoutMethod {
  SWEDISH_BANK_ID,
  NORWEGIAN_BANK_ID,
  DANISH_BANK_ID,
  SIMPLE_SIGN,
  APPROVE_ONLY,
  UNKNOWN,
}

fun CheckoutMethod.checkoutIconRes() = when (this) {
  CheckoutMethod.SWEDISH_BANK_ID -> R.drawable.ic_bank_id
  CheckoutMethod.SIMPLE_SIGN,
  CheckoutMethod.APPROVE_ONLY,
  CheckoutMethod.NORWEGIAN_BANK_ID, // Deprecated
  CheckoutMethod.DANISH_BANK_ID, // Deprecated
  CheckoutMethod.UNKNOWN,
  -> null
}

fun giraffe.type.CheckoutMethod.toCheckoutMethod() = when (this) {
  giraffe.type.CheckoutMethod.SWEDISH_BANK_ID -> CheckoutMethod.SWEDISH_BANK_ID
  giraffe.type.CheckoutMethod.NORWEGIAN_BANK_ID -> CheckoutMethod.NORWEGIAN_BANK_ID
  giraffe.type.CheckoutMethod.DANISH_BANK_ID -> CheckoutMethod.DANISH_BANK_ID
  giraffe.type.CheckoutMethod.SIMPLE_SIGN -> CheckoutMethod.SIMPLE_SIGN
  giraffe.type.CheckoutMethod.APPROVE_ONLY -> CheckoutMethod.APPROVE_ONLY
  giraffe.type.CheckoutMethod.UNKNOWN__ -> CheckoutMethod.UNKNOWN
}
