package com.hedvig.app.feature.offer.model

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.core.common.android.QuoteCartId

data class OfferModel(
  val id: QuoteCartId?,
  val variants: List<QuoteBundleVariant>,
  val checkoutMethod: CheckoutMethod,
  val campaign: Campaign?,
  val checkout: Checkout?,
  val paymentMethodsApiResponse: PaymentMethodsApiResponse?,
)
