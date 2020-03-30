package com.hedvig.app.mocks

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.PerilCategoryFragment
import com.hedvig.android.owldroid.fragment.SignStatusFragment
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.graphql.SignOfferMutation
import com.hedvig.android.owldroid.type.InsuranceStatus
import com.hedvig.android.owldroid.type.InsuranceType
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
            OfferQuery.Insurance(
                status = InsuranceStatus.PENDING,
                address = "Testvägen 1",
                personsInHousehold = 2,
                previousInsurer = OfferQuery.PreviousInsurer(
                    displayName = "Folksam",
                    switchable = true
                ),
                livingSpace = 42,
                type = InsuranceType.BRF,
                presaleInformationUrl = "http://www.africau.edu/images/default/sample.pdf",
                policyUrl = "http://www.africau.edu/images/default/sample.pdf",
                ancillaryArea = null,
                yearOfConstruction = null,
                numberOfBathrooms = null,
                extraBuildings = null,
                isSubleted = null,
                arrangedPerilCategories = OfferQuery.ArrangedPerilCategories(
                    me = OfferQuery.Me(
                        fragments = OfferQuery.Me.Fragments(
                            PerilCategoryFragment(
                                title = "Mock",
                                description = "Mock",
                                iconUrl = null,
                                perils = listOf(
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    ),
                                    PerilCategoryFragment.Peril(
                                        id = "ME.LEGAL",
                                        title = "Mock",
                                        description = "Mock"
                                    )
                                )
                            )
                        )
                    ),
                    home = null,
                    stuff = null
                ),
                cost = OfferQuery.Cost(
                    "InsuranceCost",
                    OfferQuery.Cost.Fragments(
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

                )
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
                                asMonthlyCostDeduction = null
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
                        id = "ea656f5f-40b2-4953-85d9-752b33e69e38"
                    )
                )
            )
        )

        private val UNSIGNED_WITH_HOUSE = OfferQuery.Data(
            OfferQuery.Insurance(
                status = InsuranceStatus.PENDING,
                address = "Testvägen 1",
                personsInHousehold = 2,
                previousInsurer = OfferQuery.PreviousInsurer(
                    displayName = "Folksam",
                    switchable = true
                ),
                livingSpace = 42,
                type = InsuranceType.HOUSE,
                presaleInformationUrl = "http://www.africau.edu/images/default/sample.pdf",
                policyUrl = "http://www.africau.edu/images/default/sample.pdf",
                ancillaryArea = 30,
                yearOfConstruction = 1992,
                numberOfBathrooms = 2,
                extraBuildings = emptyList(),
                isSubleted = true,
                arrangedPerilCategories = OfferQuery.ArrangedPerilCategories(
                    me = null,
                    home = null,
                    stuff = null
                ),
                cost = null
            ),
            emptyList(),
            OfferQuery.LastQuoteOfMember(
                asCompleteQuote = OfferQuery.AsCompleteQuote(
                    startDate = LocalDate.of(2020, 2, 1),
                    id = "ea656f5f-40b2-4953-85d9-752b33e69e38",
                    currentInsurer = OfferQuery.CurrentInsurer(
                        id = "ea656f5f-40b2-4953-85d9-752b33e69e38"
                    )
                )
            )
        )
    }
}
