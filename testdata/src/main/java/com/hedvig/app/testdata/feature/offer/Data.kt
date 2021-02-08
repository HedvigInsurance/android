package com.hedvig.app.testdata.feature.offer

import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.feature.offer.builders.OfferDataBuilder

val OFFER_DATA_SWEDISH_APARTMENT = OfferDataBuilder().build()

val OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE = OfferDataBuilder(
    currentInsurer = OfferQuery.CurrentInsurer(
        id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080c",
        displayName = "Annat Försäkringsbolag",
        switchable = true
    )
).build()

val OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE = OfferDataBuilder(
    currentInsurer = OfferQuery.CurrentInsurer(
        id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080d",
        displayName = "Annat Försäkringsbolag",
        switchable = false
    )
).build()

val OFFER_DATA_SWEDISH_HOUSE = OfferDataBuilder(
    typeOfContract = TypeOfContract.SE_HOUSE,
    variant = OfferDataBuilder.Variant.SWEDISH_HOUSE,
    extraBuildings = listOf(
        OfferQuery.ExtraBuilding(
            asExtraBuildingCore = OfferQuery.AsExtraBuildingCore(
                displayName = "Extrabyggnad",
                area = 10,
                hasWaterConnected = false
            )
        )
    )
).build()
