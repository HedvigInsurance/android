package com.hedvig.app.feature.offer.model

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.model.quotebundle.QuoteBundle
import com.hedvig.app.feature.offer.model.quotebundle.toQuoteBundle
import com.hedvig.app.feature.offer.ui.checkoutLabel

data class OfferModel(
    val quoteBundle: QuoteBundle,
    val checkoutMethod: CheckoutMethod,
    val checkoutLabel: CheckoutLabel,
    val campaign: Campaign?,
    val checkout: Checkout?,
    val paymentConnection: PaymentConnection?,
)

fun OfferModel.paymentApiResponseOrNull(): PaymentMethodsApiResponse? {
    return paymentConnection
        ?.providers
        ?.filterIsInstance(PaymentProvider.Adyen::class.java)
        ?.firstOrNull()
        ?.availablePaymentOptions
}

fun OfferQuery.Data.toOfferModel() = OfferModel(
    quoteBundle = quoteBundle.fragments.quoteBundleFragment.toQuoteBundle(),
    checkoutMethod = signMethodForQuotes.toCheckoutMethod(),
    checkoutLabel = checkoutLabel(),
    campaign = Campaign(
        displayValue = redeemedCampaigns
            .firstNotNullOfOrNull { it.fragments.incentiveFragment.displayValue },
        incentive = redeemedCampaigns.firstOrNull()?.fragments?.incentiveFragment?.incentive?.toIncentive()
            ?: Campaign.Incentive.NoDiscount
    ),
    checkout = Checkout(
        status = Checkout.CheckoutStatus.FAILED,
        statusText = null,
        redirectUrl = null
    ),
    paymentConnection = null,
)

fun QuoteCartFragment.toOfferModel() = OfferModel(
    quoteBundle = bundle!!.fragments.quoteBundleFragment.toQuoteBundle(),
    checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
    checkoutLabel = checkoutLabel(),
    campaign = campaign?.toCampaign(),
    checkout = checkout?.toCheckout(),
    paymentConnection = paymentConnection?.toPaymentConnection(),
)
