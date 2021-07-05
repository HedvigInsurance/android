package com.hedvig.app.testdata.feature.offer

import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.InsuranceTermType
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.offer.builders.ConcurrentInceptionBuilder
import com.hedvig.app.testdata.feature.offer.builders.IndependentInceptionBuilder
import com.hedvig.app.testdata.feature.offer.builders.OfferDataBuilder
import java.time.LocalDate

val OFFER_DATA_SWEDISH_APARTMENT = OfferDataBuilder().build()

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
    ).build(),
    insurableLimits = listOf(
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit",
                    limit = "Limit",
                    description = "Description"
                )
            )
        ),
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit 2",
                    limit = "Limit 2",
                    description = "Description 2"
                )
            )
        ),
    ),
    insuranceTerms = listOf(
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.GENERAL_TERMS,
            displayName = "General term",
            url = "invalid url"
        ),
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.TERMS_AND_CONDITIONS,
            displayName = "Terms and conditions",
            url = "invalid url"
        )
    )
).build()

val OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT = OfferDataBuilder(
    typeOfContract = TypeOfContract.SE_HOUSE,
    redeemedCampaigns = listOf(
        OfferQuery.RedeemedCampaign(
            fragments = OfferQuery.RedeemedCampaign.Fragments(
                incentiveFragment = IncentiveFragment(
                    incentive = IncentiveFragment.Incentive(
                        asMonthlyCostDeduction = IncentiveFragment.AsMonthlyCostDeduction(
                            amount = IncentiveFragment.Amount(
                                amount = "30"
                            )
                        ),
                        asPercentageDiscountMonths = null,
                        asNoDiscount = null,
                        asFreeMonths = null
                    ),
                    displayValue = "10 SEK DISCOUNT/MO."
                )
            )
        )
    )
).build()
val CONCURRENT_INCEPTION_START_DATE: LocalDate = LocalDate.of(2021, 6, 22)
val BUNDLE_WITH_CONCURRENT_INCEPTION_DATES_SPECIFIC_DATE = OfferDataBuilder(
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
    ).build(),
    insurableLimits = listOf(
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit",
                    limit = "Limit",
                    description = "Description"
                )
            )
        ),
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit 2",
                    limit = "Limit 2",
                    description = "Description 2"
                )
            )
        ),
    ),
    redeemedCampaigns = listOf(),
    inceptions = ConcurrentInceptionBuilder(startDate = CONCURRENT_INCEPTION_START_DATE).build(),
    insuranceTerms = listOf(
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.GENERAL_TERMS,
            displayName = "General term",
            url = "invalid url"
        ),
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.TERMS_AND_CONDITIONS,
            displayName = "Terms and conditions",
            url = "invalid url"
        )
    )
).build()

val BUNDLE_WITH_CONCURRENT_INCEPTION_DATES = OfferDataBuilder(
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
    ).build(),
    insurableLimits = listOf(
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit",
                    limit = "Limit",
                    description = "Description"
                )
            )
        ),
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit 2",
                    limit = "Limit 2",
                    description = "Description 2"
                )
            )
        ),
    ),
    redeemedCampaigns = listOf(),
    inceptions = ConcurrentInceptionBuilder().build(),
    signMethod = SignMethod.APPROVE_ONLY
).build()

val BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES = OfferDataBuilder(
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
    ),
    inceptions = IndependentInceptionBuilder().build(),
    insuranceTerms = listOf(
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.GENERAL_TERMS,
            displayName = "General term",
            url = "invalid url"
        ),
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.TERMS_AND_CONDITIONS,
            displayName = "Terms and conditions",
            url = "invalid url"
        )
    )
).build()

val BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER = OfferDataBuilder(
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
    ).build(),
    insurableLimits = listOf(
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit",
                    limit = "Limit",
                    description = "Description"
                )
            )
        ),
        OfferQuery.InsurableLimit(
            fragments = OfferQuery.InsurableLimit.Fragments(
                InsurableLimitsFragment(
                    label = "Test insurable limit 2",
                    limit = "Limit 2",
                    description = "Description 2"
                )
            )
        ),
    ),
    redeemedCampaigns = listOf(),
    inceptions = IndependentInceptionBuilder(startDateFromPreviousInsurer = true).build(),
    insuranceTerms = listOf(
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.GENERAL_TERMS,
            displayName = "General term",
            url = "invalid url"
        ),
        OfferQuery.InsuranceTerm(
            type = InsuranceTermType.TERMS_AND_CONDITIONS,
            displayName = "Terms and conditions",
            url = "invalid url"
        )
    ),
    signMethod = SignMethod.SIMPLE_SIGN
).build()
