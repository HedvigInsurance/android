package com.hedvig.app.mocks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.ApartmentType
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.DevelopmentActivity
import com.hedvig.app.feature.offer.OfferViewModel
import org.threeten.bp.LocalDate

class MockOfferViewModel(
    context: Context
) : OfferViewModel() {
    override val data = MutableLiveData<OfferQuery.Data>()
    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        val activePersona = context
            .getSharedPreferences(DevelopmentActivity.DEVELOPMENT_PREFERENCES, Context.MODE_PRIVATE)
            .getInt("mockPersona", 0)

        data.postValue(
            when (activePersona) {
                0 -> UNSIGNED_WITH_APARTMENT
                1 -> UNSIGNED_WITH_HOUSE
                else -> UNSIGNED_WITH_APARTMENT
            }
        )
    }

    override fun removeDiscount() = Unit
    override fun writeDiscountToCache(data: RedeemReferralCodeMutation.Data) = Unit
    override fun triggerOpenChat(done: () -> Unit) = Unit
    override fun startSign() = Unit
    override fun clearPreviousErrors() = Unit
    override fun manuallyRecheckSignStatus() = Unit
    override fun chooseStartDate(id: String, date: LocalDate) = Unit
    override fun removeStartDate(id: String) {
    }

    companion object {
        private val UNSIGNED_WITH_APARTMENT = OfferQuery.Data(
            insurance = OfferQuery.Insurance(
                status = InsuranceStatus.PENDING
            ),
            redeemedCampaigns = listOf(
                OfferQuery.RedeemedCampaign(
                    fragments = OfferQuery.RedeemedCampaign.Fragments(
                        IncentiveFragment(
                            incentive = IncentiveFragment.Incentive(
                                asPercentageDiscountMonths = IncentiveFragment.AsPercentageDiscountMonths(
                                    percentageDiscount = 50.0,
                                    pdmQuantity = 3
                                ),
                                asFreeMonths = null,
                                asMonthlyCostDeduction = null,
                                asNoDiscount = null
                            )
                        )
                    )
                )
            ),
            lastQuoteOfMember = OfferQuery.LastQuoteOfMember(
                asCompleteQuote = OfferQuery.AsCompleteQuote(
                    startDate = LocalDate.of(2020, 2, 1),
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                    currentInsurer = OfferQuery.CurrentInsurer(
                        id = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                        displayName = "Folksam",
                        switchable = true
                    ),
                    quoteDetails = OfferQuery.QuoteDetails(
                        asSwedishApartmentQuoteDetails = OfferQuery.AsSwedishApartmentQuoteDetails(
                            type = ApartmentType.BRF,
                            street = "Testvägen 1",
                            zipCode = "12345",
                            householdSize = 2,
                            livingSpace = 42
                        ),
                        asSwedishHouseQuoteDetails = null
                    ),
                    insuranceCost = OfferQuery.InsuranceCost(
                        "InsuranceCost",
                        OfferQuery.InsuranceCost.Fragments(
                            CostFragment(
                                "InsuranceCost",
                                CostFragment.MonthlyDiscount(
                                    "MonetaryAmountV2",
                                    "50.0"
                                ),
                                CostFragment.MonthlyNet(
                                    "MonetaryAmountV2",
                                    "50.0"
                                ),
                                CostFragment.MonthlyGross(
                                    "MonetaryAmountV2",
                                    "100.0"
                                )
                            )
                        )
                    ),
                    perils = listOf(),
                    termsAndConditions = OfferQuery.TermsAndConditions(
                        displayName = "TermsAndConditions",
                        url = "https://www.example.com/"
                    ),
                    insurableLimits = listOf(),
                    typeOfContract = TypeOfContract.SE_APARTMENT_BRF
                    // insuranceTerms = listOf()
                )
            )
        )

        private val UNSIGNED_WITH_HOUSE = OfferQuery.Data(
            insurance = OfferQuery.Insurance(
                status = InsuranceStatus.PENDING
            ),
            redeemedCampaigns = listOf(
                OfferQuery.RedeemedCampaign(
                    fragments = OfferQuery.RedeemedCampaign.Fragments(
                        IncentiveFragment(
                            incentive = IncentiveFragment.Incentive(
                                asPercentageDiscountMonths = IncentiveFragment.AsPercentageDiscountMonths(
                                    percentageDiscount = 50.0,
                                    pdmQuantity = 3
                                ),
                                asFreeMonths = null,
                                asMonthlyCostDeduction = null,
                                asNoDiscount = null
                            )
                        )
                    )
                )
            ),
            lastQuoteOfMember = OfferQuery.LastQuoteOfMember(
                asCompleteQuote = OfferQuery.AsCompleteQuote(
                    startDate = LocalDate.of(2020, 2, 1),
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                    currentInsurer = OfferQuery.CurrentInsurer(
                        id = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                        displayName = "Folksam",
                        switchable = true
                    ),
                    quoteDetails = OfferQuery.QuoteDetails(
                        asSwedishApartmentQuoteDetails = null,
                        asSwedishHouseQuoteDetails = OfferQuery.AsSwedishHouseQuoteDetails(
                            street = "Testvägen 1",
                            zipCode = "12345",
                            householdSize = 2,
                            livingSpace = 42,
                            ancillarySpace = 30,
                            yearOfConstruction = 1992,
                            numberOfBathrooms = 2,
                            isSubleted = true,
                            extraBuildings = emptyList()
                        )
                    ),
                    insuranceCost = OfferQuery.InsuranceCost(
                        "InsuranceCost",
                        OfferQuery.InsuranceCost.Fragments(
                            CostFragment(
                                "InsuranceCost",
                                CostFragment.MonthlyDiscount(
                                    "MonetaryAmountV2",
                                    "50.0"
                                ),
                                CostFragment.MonthlyNet(
                                    "MonetaryAmountV2",
                                    "50.0"
                                ),
                                CostFragment.MonthlyGross(
                                    "MonetaryAmountV2",
                                    "100.0"
                                )
                            )
                        )
                    ),
                    perils = listOf(),
                    termsAndConditions = OfferQuery.TermsAndConditions(
                        displayName = "TermsAndConditions",
                        url = "https://www.example.com/"
                    ),
                    insurableLimits = listOf(),
                    typeOfContract = TypeOfContract.SE_APARTMENT_BRF
                    // insuranceTerms = listOf()
                )
            )
        )
    }
}
