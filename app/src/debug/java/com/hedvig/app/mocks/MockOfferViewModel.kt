package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.ApartmentType
import com.hedvig.android.owldroid.type.TypeOfContract
import com.hedvig.app.feature.offer.OfferViewModel
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import org.threeten.bp.LocalDate

class MockOfferViewModel : OfferViewModel() {
    override val data = MutableLiveData<OfferQuery.Data>()
    override val autoStartToken = MutableLiveData<SignOfferMutation.Data>()
    override val signStatus = MutableLiveData<SignStatusFragment>()
    override val signError = MutableLiveData<Boolean>()

    init {
        data.postValue(mockData)
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
        var mockData = OFFER_DATA_SWEDISH_APARTMENT

        private val UNSIGNED_WITH_APARTMENT = OfferQuery.Data(
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
                        fragments = OfferQuery.InsuranceCost.Fragments(
                            CostFragment(
                                "InsuranceCost",
                                CostFragment.MonthlyDiscount(
                                    fragments = CostFragment.MonthlyDiscount.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "50.0",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                CostFragment.MonthlyNet(
                                    fragments = CostFragment.MonthlyNet.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "50.0",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                CostFragment.MonthlyGross(
                                    fragments = CostFragment.MonthlyGross.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "100.0",
                                            currency = "SEK"
                                        )
                                    )
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
                    typeOfContract = TypeOfContract.SE_APARTMENT_BRF,
                    insuranceTerms = listOf()
                )
            ),
            contracts = listOf()
        )

        private val UNSIGNED_WITH_HOUSE = OfferQuery.Data(
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
                        fragments = OfferQuery.InsuranceCost.Fragments(
                            CostFragment(
                                monthlyDiscount = CostFragment.MonthlyDiscount(
                                    fragments = CostFragment.MonthlyDiscount.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "50.0",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                monthlyNet = CostFragment.MonthlyNet(
                                    fragments = CostFragment.MonthlyNet.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "50.0",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                monthlyGross = CostFragment.MonthlyGross(
                                    fragments = CostFragment.MonthlyGross.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "100.0",
                                            currency = "SEK"
                                        )
                                    )
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
                    typeOfContract = TypeOfContract.SE_APARTMENT_BRF,
                    insuranceTerms = listOf()
                )
            ),
            contracts = listOf()
        )
    }
}
