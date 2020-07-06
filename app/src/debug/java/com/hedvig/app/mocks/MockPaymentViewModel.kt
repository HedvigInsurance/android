package com.hedvig.app.mocks

import androidx.lifecycle.MutableLiveData
import com.hedvig.android.owldroid.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.fragment.ContractStatusFragment
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.IncentiveFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.feature.profile.ui.payment.PaymentViewModel
import org.threeten.bp.LocalDate

class MockPaymentViewModel : PaymentViewModel() {
    override val data = MutableLiveData<Pair<ProfileQuery.Data?, PayinStatusQuery.Data?>>()

    init {
        data.postValue(
            Pair(
                ProfileQuery.Data(
                    ProfileQuery.Member(
                        id = "123456",
                        firstName = "Test",
                        lastName = "Testerson",
                        email = "test@hedvig.com",
                        phoneNumber = "07012334567"
                    ),
                    ProfileQuery.InsuranceCost(
                        freeUntil = null,
                        fragments = ProfileQuery.InsuranceCost.Fragments(
                            CostFragment(
                                monthlyDiscount = CostFragment.MonthlyDiscount(
                                    fragments = CostFragment.MonthlyDiscount.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "10.00",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                monthlyNet = CostFragment.MonthlyNet(
                                    fragments = CostFragment.MonthlyNet.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "99.00",
                                            currency = "SEK"
                                        )
                                    )
                                ),
                                monthlyGross = CostFragment.MonthlyGross(
                                    fragments = CostFragment.MonthlyGross.Fragments(
                                        MonetaryAmountFragment(
                                            amount = "109.00",
                                            currency = "SEK"
                                        )
                                    )
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
                    bankAccount = null,
                    /*
                    bankAccount = ProfileQuery.BankAccount(
                        bankName = "Testbanken",
                        descriptor = "*** 456 789"
                    ),
                    */
                    activePaymentMethods = ProfileQuery.ActivePaymentMethods(
                        fragments = ProfileQuery.ActivePaymentMethods.Fragments(
                            ActivePaymentMethodsFragment(
                                storedPaymentMethodsDetails = ActivePaymentMethodsFragment.StoredPaymentMethodsDetails(
                                    brand = "Mastercard",
                                    lastFourDigits = "1234",
                                    expiryMonth = "01",
                                    expiryYear = "2025"
                                )
                            )
                        )
                    ),
                    cashback = null,
                    cashbackOptions = emptyList(),
                    redeemedCampaigns = listOf(
                        ProfileQuery.RedeemedCampaign(
                            owner = null,
                            fragments = ProfileQuery.RedeemedCampaign.Fragments(
                                IncentiveFragment(
                                    incentive = IncentiveFragment.Incentive(
                                        asPercentageDiscountMonths = IncentiveFragment.AsPercentageDiscountMonths(
                                            percentageDiscount = 20.0,
                                            pdmQuantity = 1
                                        ),
                                        asMonthlyCostDeduction = null,
                                        asFreeMonths = null,
                                        asNoDiscount = null
                                    )
                                )
                            )
                        )
                    ),
                    referralInformation = ProfileQuery.ReferralInformation(
                        campaign = ProfileQuery.Campaign(
                            code = "ABC123",
                            incentive = null
                        )
                    )
                ),
                PayinStatusQuery.Data(PayinMethodStatus.NEEDS_SETUP)
            )
        )
    }
}
