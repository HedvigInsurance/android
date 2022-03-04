package com.hedvig.app.feature.offer.model

import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.android.owldroid.type.CheckoutStatus

data class Checkout(
    private val status: CheckoutStatus,
    private val statusText: String?,
    private val redirectUrl: String?,
) {
    enum class CheckoutStatus {
        COMPLETED,
        FAILED,
        PENDING,
        SIGNED,
        UNKNOWN;
    }
}

fun QuoteCartFragment.Checkout.toCheckout() = Checkout(
    status = when (status) {
        CheckoutStatus.PENDING -> Checkout.CheckoutStatus.PENDING
        CheckoutStatus.SIGNED -> Checkout.CheckoutStatus.SIGNED
        CheckoutStatus.COMPLETED -> Checkout.CheckoutStatus.COMPLETED
        CheckoutStatus.FAILED -> Checkout.CheckoutStatus.FAILED
        CheckoutStatus.UNKNOWN__ -> Checkout.CheckoutStatus.UNKNOWN
    },
    statusText = statusText,
    redirectUrl = redirectUrl,
)
