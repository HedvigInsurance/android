package com.hedvig.app.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferModel

object OfferItemsBuilder {
    fun createItems(data: OfferQuery.Data): List<OfferModel> {
        return listOfNotNull(
            OfferModel.Header(data),
            OfferModel.Info,
            OfferModel.Facts(data),
            OfferModel.Perils(data),
            OfferModel.Terms(data),
            data.lastQuoteOfMember.asCompleteQuote?.currentInsurer?.let { currentInsurer ->
                if (currentInsurer.switchable == true) {
                    OfferModel.Switcher(currentInsurer.displayName)
                } else {
                    null
                }
            }
        )
    }
}
