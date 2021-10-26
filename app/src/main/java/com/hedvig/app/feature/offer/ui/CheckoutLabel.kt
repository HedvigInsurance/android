package com.hedvig.app.feature.offer.ui

import android.content.Context
import androidx.annotation.StringRes
import com.hedvig.app.R

enum class CheckoutLabel(@StringRes val resId: Int) {
    SIGN_UP(R.string.OFFER_SIGN_BUTTON),
    CONTINUE(R.string.OFFER_CHECKOUT_BUTTON),
    APPROVE(R.string.OFFER_APPROVE_CHANGES),
    CONFIRM(R.string.OFFER_CONFIRM_PURCHASE),
    UNKNOWN(R.string.dummy_string);

    fun toString(context: Context) = context.getString(resId)

    fun localizationKey(context: Context): String = context.resources.getResourceEntryName(resId)
}
