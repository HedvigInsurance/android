package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.CostBuilder
import org.threeten.bp.LocalDate

data class OfferDataBuilder(
    private val startDate: LocalDate? = null,
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val currentInsurer: OfferQuery.CurrentInsurer? = null,
    private val quoteDetails: OfferQuery.QuoteDetails = OfferQuery.QuoteDetails(
        asSwedishApartmentQuoteDetails = SwedishApartmentQuoteDetailsBuilder().build(),
        asSwedishHouseQuoteDetails = null
    ),
    private val insuranceCost: CostFragment = CostBuilder()
        .build(),
    private val perils: List<OfferQuery.Peril> = emptyList(),
    private val termsAndConditionsUrl: String = "https://www.example.com",
    private val insurableLimits: List<OfferQuery.InsurableLimit> = emptyList(),
    private val insuranceTerms: List<OfferQuery.InsuranceTerm> = emptyList(),
    private val redeemedCampaigns: List<OfferQuery.RedeemedCampaign> = emptyList(),
    private val contracts: List<OfferQuery.Contract> = emptyList()
) {
    fun build() = OfferQuery.Data(
        lastQuoteOfMember = OfferQuery.LastQuoteOfMember(
            asCompleteQuote = OfferQuery.AsCompleteQuote(
                startDate = startDate,
                id = id,
                typeOfContract = typeOfContract,
                currentInsurer = currentInsurer,
                quoteDetails = quoteDetails,
                insuranceCost = OfferQuery.InsuranceCost(
                    fragments = OfferQuery.InsuranceCost.Fragments(
                        insuranceCost
                    )
                ),
                perils = perils,
                termsAndConditions = OfferQuery.TermsAndConditions(
                    displayName = "Villkor",
                    url = termsAndConditionsUrl
                ),
                insurableLimits = insurableLimits,
                insuranceTerms = insuranceTerms
            )
        ),
        redeemedCampaigns = redeemedCampaigns,
        contracts = contracts
    )
}

