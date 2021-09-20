package com.hedvig.app.feature.offer.ui

import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R

fun SignMethod.checkoutIconRes() = when (this) {
    SignMethod.SWEDISH_BANK_ID -> R.drawable.ic_bank_id
    SignMethod.SIMPLE_SIGN,
    SignMethod.APPROVE_ONLY,
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> null
}
