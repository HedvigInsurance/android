package com.hedvig.app

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.LiveEvent
import fragment.CostFragment
import fragment.IncentiveFragment
import org.threeten.bp.LocalDate
import type.InsuranceStatus
import type.InsuranceType

class MockProfileViewModel : ProfileViewModel() {
    override val data =
        MutableLiveData<ProfileQuery.Data>()
    override val dirty = MutableLiveData<Boolean>()
    override val trustlyUrl = LiveEvent<String>()

    init {
        data.postValue(
            ProfileQuery.Data(
                ProfileQuery.Member(
                    "Member",
                    "123456",
                    "Test",
                    "Testerson",
                    "test@hedvig.com",
                    "070 123 345 67"
                ),
                ProfileQuery.Insurance(
                    "Insurance",
                    "Testvägen 1",
                    "12345",
                    InsuranceType.BRF,
                    InsuranceStatus.ACTIVE,
                    "http://www.africau.edu/images/default/sample.pdf",
                    2,
                    50,
                    ProfileQuery.Cost(
                        "InsuranceCost",
                        //null,
                        LocalDate.of(2019, 11, 27),
                        ProfileQuery.Cost.Fragments(
                            CostFragment(
                                "InsuranceCost",
                                CostFragment.MonthlyDiscount(
                                    "MonetaryAmountV2",
                                    "10.00"
                                ),
                                CostFragment.MonthlyNet(
                                    "MonetaryAmountV2",
                                    "119.00"
                                ),
                                CostFragment.MonthlyGross(
                                    "MonetaryAmountV2",
                                    "129.00"
                                )
                            )
                        )
                    )
                ),
                ProfileQuery.Balance(
                    "Balance",
                    3
                ),
                ProfileQuery.ChargeEstimation(
                    "ChargeEstimation",
                    ProfileQuery.Charge(
                        "MonetaryAmountV2",
                        "119.00"
                    ),
                    ProfileQuery.Discount(
                        "MonetaryAmountV2",
                        "10.00"
                    ),
                    ProfileQuery.Subscription(
                        "MonetaryAmountV2",
                        "129.00"
                    )
                ),
                LocalDate.of(2019, 10, 27),
                listOf(
                    ProfileQuery.ChargeHistory(
                        "Charge",
                        ProfileQuery.Amount(
                            "MonetaryAmountV2",
                            "129.00"
                        ),
                        LocalDate.of(2019, 8, 27)
                    ),
                    ProfileQuery.ChargeHistory(
                        "Charge",
                        ProfileQuery.Amount(
                            "MonetaryAmountV2",
                            "129.00"
                        ),
                        LocalDate.of(2019, 7, 27)
                    ),
                    ProfileQuery.ChargeHistory(
                        "Charge",
                        ProfileQuery.Amount(
                            "MonetaryAmountV2",
                            "129.00"
                        ),
                        LocalDate.of(2019, 6, 27)
                    )
                ),
                ProfileQuery.BankAccount(
                    "BankAccount",
                    "Testbanken",
                    "*** 456 789"
                ),
                null,
                listOf(),
                listOf(
                    ProfileQuery.RedeemedCampaign(
                        "Campaign",
                        ProfileQuery.Owner(
                            "CampaignOwner",
                            "Test campaign"
                        ),
                        ProfileQuery.RedeemedCampaign.Fragments(
                            IncentiveFragment(
                                "Incentive",
                                IncentiveFragment.Incentive(
                                    __typename = "FreeMonths",
                                    inlineFragment = IncentiveFragment.AsFreeMonth(
                                        "FreeMonths",
                                        3
                                    )
                                )
                            )
                        )
                        // null,
                        // ProfileQuery.RedeemedCampaign.Fragments(
                        //     IncentiveFragment(
                        //         "Incentive",
                        //         IncentiveFragment.AsMonthlyCostDeduction(
                        //             "MonthlyCostDeduction",
                        //             IncentiveFragment.Amount(
                        //                 "MonetaryAmountV2",
                        //                 "10.00"
                        //             )
                        //         )
                        //     )
                        // )
                    )
                ),
                ProfileQuery.ReferralInformation(
                    "Referrals",
                    ProfileQuery.Campaign(
                        "Campaign",
                        "ABC123",
                        null
                    ),
                    null,
                    listOf()
                )
            )
        )
    }

    override fun selectCashback(id: String) = Unit
    override fun triggerFreeTextChat(done: () -> Unit) = Unit
    override fun saveInputs(emailInput: String, phoneNumberInput: String) = Unit
    override fun emailChanged(newEmail: String) = Unit
    override fun phoneNumberChanged(newPhoneNumber: String) = Unit
    override fun refreshBankAccountInfo() = Unit
    override fun updateReferralsInformation(data: RedeemReferralCodeMutation.Data) = Unit
    override fun startTrustlySession() = Unit
    override fun refreshProfile() = Unit
}
