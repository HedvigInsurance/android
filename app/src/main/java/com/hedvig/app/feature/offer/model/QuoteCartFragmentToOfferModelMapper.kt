package com.hedvig.app.feature.offer.model

import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.app.common.Mapper
import com.hedvig.app.util.featureflags.FeatureManager
import com.hedvig.app.util.featureflags.flags.Feature

class QuoteCartFragmentToOfferModelMapper(
    private val featureManager: FeatureManager,
) : Mapper<QuoteCartFragment, OfferModel> {
    override suspend fun map(from: QuoteCartFragment): OfferModel {
        val connectPaymentAtSign = featureManager.isFeatureEnabled(Feature.CONNECT_PAYMENT_AT_SIGN)
        return with(from) {
            OfferModel(
                id = QuoteCartId(id),
                variants = bundle?.possibleVariations?.map { it.toQuoteBundleVariant(QuoteCartId(id), checkoutMethods) }
                    ?: emptyList(),
                checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
                campaign = campaign?.toCampaign(),
                checkout = checkout?.toCheckout(),
                paymentMethodsApiResponse = run {
                    if (connectPaymentAtSign.not()) return@run null
                    paymentConnection?.toPaymentConnection()?.toPaymentApiResponseOrNull()
                },
            )
        }
    }

    private fun PaymentConnection?.toPaymentApiResponseOrNull(): PaymentMethodsApiResponse? {
        return this
            ?.providers
            ?.mapNotNull { paymentProvider ->
                when (paymentProvider) {
                    is PaymentProvider.Adyen -> paymentProvider
                    PaymentProvider.Trustly -> null
                }
            }
            ?.firstOrNull()
            ?.availablePaymentOptions
    }
}
