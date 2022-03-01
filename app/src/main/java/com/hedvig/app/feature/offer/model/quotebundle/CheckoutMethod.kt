package com.hedvig.app.feature.offer.model.quotebundle

import com.hedvig.android.owldroid.type.SignMethod
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
    CheckoutMethod.UNKNOWN -> null
}

fun SignMethod.toCheckoutMethod() = when (this) {
    SignMethod.SWEDISH_BANK_ID -> CheckoutMethod.SWEDISH_BANK_ID
    SignMethod.NORWEGIAN_BANK_ID -> CheckoutMethod.NORWEGIAN_BANK_ID
    SignMethod.DANISH_BANK_ID -> CheckoutMethod.DANISH_BANK_ID
    SignMethod.SIMPLE_SIGN -> CheckoutMethod.SIMPLE_SIGN
    SignMethod.APPROVE_ONLY -> CheckoutMethod.APPROVE_ONLY
    SignMethod.UNKNOWN__ -> CheckoutMethod.UNKNOWN
}

fun com.hedvig.android.owldroid.type.CheckoutMethod.toCheckoutMethod() = when (this) {
    com.hedvig.android.owldroid.type.CheckoutMethod.SWEDISH_BANK_ID -> CheckoutMethod.SWEDISH_BANK_ID
    com.hedvig.android.owldroid.type.CheckoutMethod.NORWEGIAN_BANK_ID -> CheckoutMethod.NORWEGIAN_BANK_ID
    com.hedvig.android.owldroid.type.CheckoutMethod.DANISH_BANK_ID -> CheckoutMethod.DANISH_BANK_ID
    com.hedvig.android.owldroid.type.CheckoutMethod.SIMPLE_SIGN -> CheckoutMethod.SIMPLE_SIGN
    com.hedvig.android.owldroid.type.CheckoutMethod.APPROVE_ONLY -> CheckoutMethod.APPROVE_ONLY
    com.hedvig.android.owldroid.type.CheckoutMethod.UNKNOWN__ -> CheckoutMethod.UNKNOWN
}
