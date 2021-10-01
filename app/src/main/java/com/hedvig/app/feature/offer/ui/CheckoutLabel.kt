package com.hedvig.app.feature.offer.ui

import android.content.Context
import com.hedvig.app.R

enum class CheckoutLabel {
    SIGN_UP,
    CONTINUE,
    APPROVE,
    CONFIRM,
    UNKNOWN
}

fun CheckoutLabel.toString(context: Context) = when (this) {
    CheckoutLabel.SIGN_UP -> context.getString(R.string.OFFER_SIGN_BUTTON)
    CheckoutLabel.CONTINUE -> context.getString(R.string.OFFER_CHECKOUT_BUTTON)
    CheckoutLabel.APPROVE -> context.getString(R.string.OFFER_APPROVE_CHANGES)
    CheckoutLabel.CONFIRM -> context.getString(R.string.OFFER_CONFIRM_PURCHASE)
    CheckoutLabel.UNKNOWN -> ""
}
