package com.hedvig.app.feature.offer.model

import android.os.Parcelable
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class QuoteCartId(val id: String) : Parcelable

data class OfferModel(
    val id: QuoteCartId?,
    val variants: List<QuoteBundleVariant>,
    val checkoutMethod: CheckoutMethod,
    val campaign: Campaign?,
    val checkout: Checkout?,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse?,
)
