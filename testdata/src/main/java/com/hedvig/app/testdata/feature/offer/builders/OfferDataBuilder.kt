package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.ApartmentType
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import java.time.LocalDate

data class OfferDataBuilder(
    private val startDate: LocalDate? = null,
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val currentInsurer: OfferQuery.CurrentInsurer? = null,
    private val variant: Variant = Variant.SWEDISH_APARTMENT,
    private val apartmentType: ApartmentType = ApartmentType.RENT,
    private val street: String = "Testgatan 1",
    private val zipCode: String = "123 45",
    private val householdSize: Int = 1,
    private val livingSpace: Int = 50,
    private val ancillarySpace: Int = 10,
    private val yearOfConstruction: Int = 1999,
    private val isSubleted: Boolean = false,
    private val numberOfBathrooms: Int = 1,
    private val extraBuildings: List<OfferQuery.ExtraBuilding> = emptyList(),
    private val insuranceCost: CostFragment = CostBuilder()
        .build(),
    private val perils: List<OfferQuery.Peril> = PerilBuilder().offerQueryBuild(),
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
                quoteDetails = OfferQuery.QuoteDetails(
                    asSwedishApartmentQuoteDetails = if (variant == Variant.SWEDISH_APARTMENT) {
                        OfferQuery.AsSwedishApartmentQuoteDetails(
                            type = apartmentType,
                            street = street,
                            zipCode = zipCode,
                            householdSize = householdSize,
                            livingSpace = livingSpace
                        )
                    } else {
                        null
                    },
                    asSwedishHouseQuoteDetails = if (variant == Variant.SWEDISH_HOUSE) {
                        OfferQuery.AsSwedishHouseQuoteDetails(
                            ancillarySpace = ancillarySpace,
                            street = street,
                            zipCode = zipCode,
                            householdSize = householdSize,
                            livingSpace = livingSpace,
                            yearOfConstruction = yearOfConstruction,
                            isSubleted = isSubleted,
                            numberOfBathrooms = numberOfBathrooms,
                            extraBuildings = extraBuildings
                        )
                    } else {
                        null
                    }
                ),
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

    enum class Variant {
        SWEDISH_APARTMENT,
        SWEDISH_HOUSE
    }
}

