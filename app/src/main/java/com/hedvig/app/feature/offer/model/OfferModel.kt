package com.hedvig.app.feature.offer.model

import android.os.Parcelable
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
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
    id = null,
    variants = emptyList(),
    checkoutMethod = signMethodForQuotes.toCheckoutMethod(),
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
    id = QuoteCartId(id),
    variants = bundle?.possibleVariations?.map { it.toQuoteBundleVariant(QuoteCartId(id), checkoutMethods) }
        ?: emptyList(),
    checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
    campaign = campaign?.toCampaign(),
    checkout = checkout?.toCheckout(),
    paymentConnection = paymentConnection?.toPaymentConnection(),
)
