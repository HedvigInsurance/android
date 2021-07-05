package com.hedvig.app.feature.offer.ui

import android.widget.Button
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.app.R
import java.lang.IllegalArgumentException

fun SignMethod.checkoutTextRes() = when (this) {
    SignMethod.SWEDISH_BANK_ID -> R.string.OFFER_SIGN_BUTTON
    SignMethod.SIMPLE_SIGN -> R.string.OFFER_CHECKOUT_BUTTON
    SignMethod.APPROVE_ONLY -> R.string.OFFER_APPROVE_CHANGES
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> throw IllegalArgumentException("Could not parse checkout string for $this")
}

fun SignMethod.checkoutIconRes() = when (this) {
    SignMethod.SWEDISH_BANK_ID -> R.drawable.ic_bank_id
    SignMethod.SIMPLE_SIGN,
    SignMethod.APPROVE_ONLY,
    SignMethod.NORWEGIAN_BANK_ID, // Deprecated
    SignMethod.DANISH_BANK_ID, // Deprecated
    SignMethod.UNKNOWN__ -> null
}

fun MaterialButton.bindWithSignMethod(signMethod: SignMethod) {
    text = context.getString(signMethod.checkoutTextRes())
    signMethod.checkoutIconRes()?.let {
        val icon = ContextCompat.getDrawable(context, it)
        setIcon(icon)
    }
}
