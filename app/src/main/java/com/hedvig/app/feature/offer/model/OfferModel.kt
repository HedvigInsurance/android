package com.hedvig.app.feature.offer.model

import com.hedvig.app.feature.offer.model.quotebundle.Campaign
import com.hedvig.app.feature.offer.model.quotebundle.CheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.ui.CheckoutLabel

data class OfferModel(
    val quoteBundle: QuoteBundle,
    val checkoutMethod: CheckoutMethod,
    val checkoutLabel: CheckoutLabel,
    val campaign: Campaign?
)
