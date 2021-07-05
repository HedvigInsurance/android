package com.hedvig.app.testdata.feature.offer.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.TableFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.type.SignMethod
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.testdata.common.builders.CostBuilder
import com.hedvig.app.testdata.common.builders.TableFragmentBuilder
import com.hedvig.app.testdata.feature.insurance.builders.PerilBuilder
import java.time.LocalDate

data class OfferDataBuilder(
    private val startDate: LocalDate? = null,
    private val id: String = "ea656f5f-40b2-4953-85d9-752b33e69e38",
    private val typeOfContract: TypeOfContract = TypeOfContract.SE_APARTMENT_RENT,
    private val currentInsurer: OfferQuery.CurrentInsurer? = null,
    private val insuranceCost: CostFragment = CostBuilder()
        .build(),
    private val perils: List<OfferQuery.Peril> = PerilBuilder().offerQueryBuild(5),
    private val termsAndConditionsUrl: String = "https://www.example.com",
    private val insurableLimits: List<OfferQuery.InsurableLimit> = emptyList(),
    private val insuranceTerms: List<OfferQuery.InsuranceTerm> = emptyList(),
    private val redeemedCampaigns: List<OfferQuery.RedeemedCampaign> = emptyList(),
    private val contracts: List<OfferQuery.Contract> = emptyList(),
    private val detailsTable: TableFragment = TableFragmentBuilder().build(),
    private val inceptions: OfferQuery.Inception1 = ConcurrentInceptionBuilder().build(),
    private val signMethod: SignMethod = SignMethod.SWEDISH_BANK_ID
) {
    fun build() = OfferQuery.Data(
        quoteBundle = OfferQuery.QuoteBundle(
            quotes = listOf(
                OfferQuery.Quote(
                    displayName = "Test insurance",
                    startDate = startDate,
                    id = id,
                    typeOfContract = typeOfContract,
                    currentInsurer = currentInsurer,
                    detailsTable = OfferQuery.DetailsTable(
                        fragments = OfferQuery.DetailsTable.Fragments(detailsTable),
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
            bundleCost = OfferQuery.BundleCost(
                fragments = OfferQuery.BundleCost.Fragments(insuranceCost)
            ),
            inception = inceptions
        ),
        redeemedCampaigns = redeemedCampaigns,
        contracts = contracts,
        signMethodForQuotes = signMethod
    )
}
