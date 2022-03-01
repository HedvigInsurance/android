package com.hedvig.app.feature.offer.model

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.QuoteCartSubscription
import com.hedvig.app.feature.offer.model.quotebundle.Campaign
import com.hedvig.app.feature.offer.model.quotebundle.CheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.model.quotebundle.toCampaign
import com.hedvig.app.feature.offer.model.quotebundle.toCheckoutMethod
import com.hedvig.app.feature.offer.model.quotebundle.toIncentive
import com.hedvig.app.feature.offer.model.quotebundle.toQuoteBundle
import com.hedvig.app.feature.offer.ui.CheckoutLabel
import com.hedvig.app.feature.offer.ui.checkoutLabel

data class OfferModel(
    val quoteBundle: QuoteBundle,
    val checkoutMethod: CheckoutMethod,
    val checkoutLabel: CheckoutLabel,
    val campaign: Campaign?
)

fun OfferQuery.Data.toOfferModel() = OfferModel(
    quoteBundle = quoteBundle.fragments.quoteBundleFragment.toQuoteBundle(),
    checkoutMethod = signMethodForQuotes.toCheckoutMethod(),
    checkoutLabel = checkoutLabel(),
    campaign = Campaign(
        displayValue = redeemedCampaigns
            .firstNotNullOfOrNull { it.fragments.incentiveFragment.displayValue },
        incentive = redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive?.toIncentive()
            ?: Campaign.Incentive.NoDiscount
    )
)

fun QuoteCartSubscription.QuoteCart.toOfferModel() = OfferModel(
    quoteBundle = bundle!!.fragments.quoteBundleFragment.toQuoteBundle(),
    checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
    checkoutLabel = checkoutLabel(),
    campaign = campaign?.toCampaign()
)
