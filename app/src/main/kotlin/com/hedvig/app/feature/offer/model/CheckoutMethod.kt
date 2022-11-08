package com.hedvig.app.feature.offer.model

import com.hedvig.app.R

enum class CheckoutMethod {
  SWEDISH_BANK_ID,
  NORWEGIAN_BANK_ID,
  DANISH_BANK_ID,
  SIMPLE_SIGN,
  APPROVE_ONLY,
  UNKNOWN;
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

fun com.hedvig.android.apollo.graphql.type.CheckoutMethod.toCheckoutMethod() = when (this) {
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.SWEDISH_BANK_ID -> CheckoutMethod.SWEDISH_BANK_ID
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.NORWEGIAN_BANK_ID -> CheckoutMethod.NORWEGIAN_BANK_ID
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.DANISH_BANK_ID -> CheckoutMethod.DANISH_BANK_ID
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.SIMPLE_SIGN -> CheckoutMethod.SIMPLE_SIGN
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.APPROVE_ONLY -> CheckoutMethod.APPROVE_ONLY
  com.hedvig.android.apollo.graphql.type.CheckoutMethod.UNKNOWN__ -> CheckoutMethod.UNKNOWN
}
