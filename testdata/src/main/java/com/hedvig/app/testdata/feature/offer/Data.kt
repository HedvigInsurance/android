package com.hedvig.app.testdata.feature.offer

import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
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
    detailsTable = TableFragmentBuilder(
        title = "Home Insurance",
        sections = listOf(
            "Details" to listOf(
                Triple("Address", null, "Askersgatan 129 B"),
                Triple("Postal code", null, "113 89"),
                Triple("Insured people", null, "You + 2"),
                Triple("Living space", null, "180 m2"),
                Triple("Ancillary area", null, "48 m2"),
                Triple("Year built", null, "1997"),
                Triple("Number of baths", null, "2"),
                Triple("Partly subleted", null, "No"),
            ),
            "Extra buildings" to listOf(
                Triple("Garage", null, "16 m2"),
                Triple("Attefalls house", "Has water connected", "23 m2"),
            )
        )
    ).build()
).build()

val OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT = OfferDataBuilder(
    typeOfContract = TypeOfContract.SE_HOUSE,
    redeemedCampaigns = listOf(
        OfferQuery.RedeemedCampaign(
            fragments = OfferQuery.RedeemedCampaign.Fragments(
                incentiveFragment = IncentiveFragment(
                    incentive = null,
                    displayValue = "10 SEK DISCOUNT/MO."
                )
            )
        )
    )
).build()
