package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.ApartmentType

data class SwedishApartmentQuoteDetailsBuilder(
    private val type: ApartmentType = ApartmentType.RENT,
    private val street: String = "Testgatan 1",
    private val zipCode: String = "123 45",
    private val householdSize: Int = 1,
    private val livingSpace: Int = 50
) {
    fun build() =
        OfferQuery.AsSwedishApartmentQuoteDetails(
            type = type,
            street = street,
            zipCode = zipCode,
            householdSize = householdSize,
            livingSpace = livingSpace
        )
}
