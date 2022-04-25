package com.hedvig.app.feature.offer.model

import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.common.Mapper
import com.hedvig.app.feature.adyen.AdyenRepository
import com.hedvig.app.feature.offer.model.quotebundle.toQuoteBundle
import com.hedvig.app.feature.offer.ui.checkoutLabel
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

class OfferQueryDataToOfferModelMapper(
    private val featureManager: FeatureManager,
    private val adyenRepository: AdyenRepository,
) : Mapper<OfferQuery.Data, OfferModel> {
    override suspend fun map(from: OfferQuery.Data): OfferModel {
        return with(from) {
            OfferModel(
                id = null,
                quoteBundle = quoteBundle.fragments.quoteBundleFragment.toQuoteBundle(null),
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
                paymentMethodsApiResponse = if (featureManager.isFeatureEnabled(Feature.CONNECT_PAYMENT_AT_SIGN)) {
                    adyenRepository.paymentMethodsResponse()
                } else {
                    null
                }
            )
        }
    }
}

class QuoteCartFragmentToOfferModelMapper(
    private val featureManager: FeatureManager,
) : Mapper<QuoteCartFragment, OfferModel> {
    override suspend fun map(from: QuoteCartFragment): OfferModel {
        return with(from) {
            OfferModel(
                id = QuoteCartId(id),
                quoteBundle = bundle!!.fragments.quoteBundleFragment.toQuoteBundle(QuoteCartId(id)),
                checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
                checkoutLabel = checkoutLabel(),
                campaign = campaign?.toCampaign(),
                checkout = checkout?.toCheckout(),
                paymentMethodsApiResponse = if (featureManager.isFeatureEnabled(Feature.CONNECT_PAYMENT_AT_SIGN)) {
                    paymentConnection
                        ?.toPaymentConnection()
                        ?.providers
                        ?.mapNotNull { paymentProvider ->
                            when (paymentProvider) {
                                is PaymentProvider.Adyen -> paymentProvider
                                PaymentProvider.Trustly -> null
                            }
                        }
                        ?.firstOrNull()
                        ?.availablePaymentOptions
                } else {
                    null
                },
            )
        }
    }
}
