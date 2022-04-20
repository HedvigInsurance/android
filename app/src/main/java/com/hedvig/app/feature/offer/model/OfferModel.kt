package com.hedvig.app.feature.offer.model

import android.os.Parcelable
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import kotlinx.parcelize.Parcelize

@JvmInline
@Parcelize
value class QuoteCartId(val id: String) : Parcelable

data class OfferModel(
    val id: QuoteCartId?,
    val quoteBundle: QuoteBundle,
    val checkoutMethod: CheckoutMethod,
    val checkoutLabel: CheckoutLabel,
    val campaign: Campaign?,
    val checkout: Checkout?,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse?,
) {
    val externalProviderId = quoteBundle
        .quotes
        .firstNotNullOfOrNull(QuoteBundle.Quote::dataCollectionId)
}
