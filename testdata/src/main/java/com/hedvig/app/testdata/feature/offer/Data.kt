package com.hedvig.app.testdata.feature.offer

import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.InsurableLimitsFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.InsuranceTermType
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.offer.builders.ConcurrentInceptionBuilder
import com.hedvig.app.testdata.feature.offer.builders.FaqBuilder
import com.hedvig.app.testdata.feature.offer.builders.IndependentInceptionBuilder
import com.hedvig.app.testdata.feature.offer.builders.OfferDataBuilder
import com.hedvig.app.testdata.feature.offer.builders.QuoteBuilder
import java.time.LocalDate

val OFFER_DATA_SWEDISH_APARTMENT = OfferDataBuilder().build()

val OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080c",
                displayName = "Annat Försäkringsbolag",
                switchable = true
            )
        ).build()
    )
).build()

val OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_NON_SWITCHABLE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080d",
                displayName = "Annat Försäkringsbolag",
                switchable = false
            )
        ).build()
    )
).build()

val OFFER_DATA_SWEDISH_HOUSE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
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
    )
).build()

val OFFER_DATA_SWEDISH_HOUSE_WITH_DISCOUNT = OfferDataBuilder(
    insuranceCost = CostBuilder(netAmount = "349.0", grossAmount = "249.0").build(),
    quotes = listOf(
        QuoteBuilder(
            typeOfContract = TypeOfContract.SE_HOUSE,
        ).build()
    ),
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
    quotes = listOf(
        QuoteBuilder(
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
    ),
    redeemedCampaigns = listOf(),
    inceptions = ConcurrentInceptionBuilder(startDate = CONCURRENT_INCEPTION_START_DATE).build(),
).build()

val BUNDLE_WITH_CONCURRENT_INCEPTION_DATES = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
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
        ).build()
    ),
    redeemedCampaigns = listOf(),
    inceptions = ConcurrentInceptionBuilder().build(),
    signMethod = SignMethod.APPROVE_ONLY
).build()

val BUNDLE_WITH_INDEPENDENT_INCEPTION_DATES = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            typeOfContract = TypeOfContract.SE_HOUSE,
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
    ),
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
).build()

val BUNDLE_WITH_START_DATE_FROM_PREVIOUS_INSURER = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
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
    ),
    redeemedCampaigns = listOf(),
    inceptions = IndependentInceptionBuilder(startDateFromPreviousInsurer = true).build()
).build()

val OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            displayName = "Home Contents Insurance",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_OWN,
        ).build(),
        QuoteBuilder(
            displayName = "Travel Insurance",
            typeOfContract = TypeOfContract.NO_TRAVEL,
        ).build()
    ),
    frequentlyAskedQuestions = listOf(
        FaqBuilder(
            headline = "Test headline",
            body = "Test body",
        ).build()
    )
).build()

val OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_NONSWITCHABLE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            displayName = "Home Contents Insurance",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_OWN,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080d",
                displayName = "Annat Försäkringsbolag",
                switchable = false
            )
        ).build(),
        QuoteBuilder(
            displayName = "Travel Insurance",
            typeOfContract = TypeOfContract.NO_TRAVEL,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080f",
                displayName = "Annat Försäkringsbolag 2",
                switchable = false
            )
        ).build()
    ),
    frequentlyAskedQuestions = listOf(
        FaqBuilder(
            headline = "Test headline",
            body = "Test body",
        ).build()
    )
).build()

val OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_ALL_SWITCHABLE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            displayName = "Home Contents Insurance",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_OWN,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080d",
                displayName = "Annat Försäkringsbolag",
                switchable = true
            )
        ).build(),
        QuoteBuilder(
            displayName = "Travel Insurance",
            typeOfContract = TypeOfContract.NO_TRAVEL,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080f",
                displayName = "Annat Försäkringsbolag 2",
                switchable = true
            )
        ).build()
    ),
    frequentlyAskedQuestions = listOf(
        FaqBuilder(
            headline = "Test headline",
            body = "Test body",
        ).build()
    )
).build()

val OFFER_DATA_NORWAY_BUNDLE_HOME_CONTENTS_TRAVEL_MULTIPLE_PREVIOUS_INSURERS_MIXED_SWITCHABLE = OfferDataBuilder(
    quotes = listOf(
        QuoteBuilder(
            displayName = "Home Contents Insurance",
            typeOfContract = TypeOfContract.NO_HOME_CONTENT_OWN,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080d",
                displayName = "Annat Försäkringsbolag",
                switchable = false
            )
        ).build(),
        QuoteBuilder(
            displayName = "Travel Insurance",
            typeOfContract = TypeOfContract.NO_TRAVEL,
            currentInsurer = OfferQuery.CurrentInsurer(
                id = "ab2a1f4c-83af-4b9f-98d2-e7ea767b080f",
                displayName = "Annat Försäkringsbolag 2",
                switchable = true
            )
        ).build()
    ),
    frequentlyAskedQuestions = listOf(
        FaqBuilder(
            headline = "Test headline",
            body = "Test body",
        ).build()
    )
).build()
