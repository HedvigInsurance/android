package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.graphql.DirectDebitQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.RedeemReferralCodeMutation
import com.hedvig.android.owldroid.type.DirectDebitStatus
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.feature.profile.ui.ProfileViewModel
import com.hedvig.app.util.LiveEvent
import org.threeten.bp.LocalDate

class MockProfileViewModel : ProfileViewModel() {
    override val data =
        MutableLiveData<ProfileQuery.Data>()
    override val dirty = MutableLiveData<Boolean>()
    override val trustlyUrl = LiveEvent<String>()
    override val directDebitStatus = MutableLiveData<DirectDebitQuery.Data>()

    init {
        data.postValue(
            ProfileQuery.Data(
                ProfileQuery.Member(
                    id = "123456",
                    firstName = "Test",
                    lastName = "Testerson",
                    email = "test@hedvig.com",
                    phoneNumber = "07012334567",
                    features = listOf(Feature.KEYGEAR)
                ),
                ProfileQuery.InsuranceCost(
                    freeUntil = null,
                    fragments = ProfileQuery.InsuranceCost.Fragments(
                        CostFragment(
                            monthlyDiscount = CostFragment.MonthlyDiscount(
                                amount = "10.00"
                            ),
                            monthlyNet = CostFragment.MonthlyNet(
                                amount = "99.00"
                            ),
                            monthlyGross = CostFragment.MonthlyGross(
                                amount = "109.00"
                            )
                        )
                    )
                ),
                listOf(
                    ProfileQuery.Contract(
                        status = ProfileQuery.Status(
                            fragments = ProfileQuery.Status.Fragments(
                                contractStatusFragment = ContractStatusFragment(
                                    asPendingStatus = null,
                                    asActiveInFutureStatus = null,
                                    asActiveStatus = ContractStatusFragment.AsActiveStatus(
                                        pastInception = LocalDate.of(2020, 2, 1)
                                    ),
                                    asActiveInFutureAndTerminatedInFutureStatus = null,
                                    asTerminatedInFutureStatus = null,
                                    asTerminatedTodayStatus = null,
                                    asTerminatedStatus = null
                                )
                            )
                        )
                    )
                ),
                //ProfileQuery.Insurance(
                //    address = "Testvägen 1",
                //    postalNumber = "12345",
                //    type = InsuranceType.BRF,
                //    status = InsuranceStatus.ACTIVE,
                //    certificateUrl = "http://www.africau.edu/images/default/sample.pdf",
                //    personsInHousehold = 2,
                //    livingSpace = 50,
                //    ancillaryArea = null,
                //    yearOfConstruction = null,
                //    numberOfBathrooms = null,
                //    cost = ProfileQuery.Cost(
                //        freeUntil = LocalDate.of(2019, 11, 27),
                //        fragments = ProfileQuery.Cost.Fragments(
                //            CostFragment(
                //                monthlyDiscount = CostFragment.MonthlyDiscount(
                //                    amount = "10.00"
                //                ),
                //                monthlyNet = CostFragment.MonthlyNet(
                //                    amount = "119.00"
                //                ),
                //                monthlyGross = CostFragment.MonthlyGross(
                //                    amount = "129.00"
                //                )
                //            )
                //        )
                //    ),
                //    extraBuildings = null,
                //    isSubleted = null
                //),
                balance = ProfileQuery.Balance(
                    failedCharges = 3
                ),
                chargeEstimation = ProfileQuery.ChargeEstimation(
                    charge = ProfileQuery.Charge(
                        amount = "119.00"
                    ),
                    discount = ProfileQuery.Discount(
                        amount = "10.00"
                    ),
                    subscription = ProfileQuery.Subscription(
                        amount = "129.00"
                    )
                ),
                nextChargeDate = LocalDate.of(2019, 10, 27),
                chargeHistory = listOf(
                    ProfileQuery.ChargeHistory(
                        amount = ProfileQuery.Amount(
                            amount = "129.00"
                        ),
                        date = LocalDate.of(2019, 8, 27)
                    ),
                    ProfileQuery.ChargeHistory(
                        amount = ProfileQuery.Amount(
                            amount = "129.00"
                        ),
                        date = LocalDate.of(2019, 7, 27)
                    ),
                    ProfileQuery.ChargeHistory(
                        amount = ProfileQuery.Amount(
                            amount = "129.00"
                        ),
                        date = LocalDate.of(2019, 6, 27)
                    )
                ),
                bankAccount = ProfileQuery.BankAccount(
                    bankName = "Testbanken",
                    descriptor = "*** 456 789"
                ),
                cashback = null,
                cashbackOptions = emptyList(),
                redeemedCampaigns = listOf(
                    ProfileQuery.RedeemedCampaign(
                        // ProfileQuery.Owner(
                        //     "CampaignOwner",
                        //     "Test campaign"
                        // ),
                        // ProfileQuery.RedeemedCampaign.Fragments(
                        //     IncentiveFragment(
                        //         "Incentive",
                        //         IncentiveFragment.AsFreeMonths(
                        //             "FreeMonths",
                        //             3
                        //         )
                        //     )
                        // )
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
                        owner = null,
                        fragments = ProfileQuery.RedeemedCampaign.Fragments(
                            IncentiveFragment(
                                incentive = IncentiveFragment.Incentive(
                                    asPercentageDiscountMonths = IncentiveFragment.AsPercentageDiscountMonths(
                                        percentageDiscount = 20.0,
                                        pdmQuantity = 1
                                    ),
                                    asMonthlyCostDeduction = null,
                                    asFreeMonths = null
                                )
                            )
                        )
                    )
                ),
                referralInformation = ProfileQuery.ReferralInformation(
                    campaign = ProfileQuery.Campaign(
                        code = "ABC123",
                        incentive = null
                    ),
                    referredBy = null,
                    invitations = emptyList()
                )
            )
        )

        directDebitStatus.postValue(DirectDebitQuery.Data(DirectDebitStatus.NEEDS_SETUP))
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
