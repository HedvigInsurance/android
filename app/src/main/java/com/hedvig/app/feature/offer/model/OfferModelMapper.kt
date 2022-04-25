package com.hedvig.app.feature.offer.model

import com.hedvig.android.owldroid.fragment.QuoteCartFragment
import com.hedvig.app.common.Mapper

class QuoteCartFragmentToOfferModelMapper : Mapper<QuoteCartFragment, OfferModel> {
    override suspend fun map(from: QuoteCartFragment): OfferModel {
        return with(from) {
            OfferModel(
                id = QuoteCartId(id),
                variants = bundle?.possibleVariations?.map { it.toQuoteBundleVariant(QuoteCartId(id), checkoutMethods) }
                    ?: emptyList(),
                checkoutMethod = checkoutMethods.map { it.toCheckoutMethod() }.first(),
                campaign = campaign?.toCampaign(),
                checkout = checkout?.toCheckout(),
                paymentConnection?.toPaymentConnection(),
            )
        }
    }
}
