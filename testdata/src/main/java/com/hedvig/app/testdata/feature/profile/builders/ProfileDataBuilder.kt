package com.hedvig.app.testdata.feature.profile.builders

import com.hedvig.android.owldroid.fragment.CashbackFragment
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.testdata.common.builders.CostBuilder
import java.time.LocalDate

data class ProfileDataBuilder(
    private val memberId: String = "123",
    private val firstName: String = "Test",
    private val lastName: String? = "Testerson",
    private val email: String? = "test@example.com",
    private val phoneNumber: String? = "07012345678",
    private val cost: CostFragment = CostBuilder().build(),
    private val contracts: List<ProfileQuery.Contract> = emptyList()
) {
    fun build() = ProfileQuery.Data(
        member = ProfileQuery.Member(
            id = memberId,
            firstName = firstName,
            lastName = lastName,
            email = email,
            phoneNumber = phoneNumber
        ),
        insuranceCost = ProfileQuery.InsuranceCost(
            freeUntil = null,
            fragments = ProfileQuery.InsuranceCost.Fragments(
                costFragment = cost
            )
        ),
        contracts = contracts,
        balance = ProfileQuery.Balance(
            failedCharges = null
        ),
        chargeEstimation = ProfileQuery.ChargeEstimation(
            charge = ProfileQuery.Charge(
                amount = "0.00"
            ),
            discount = ProfileQuery.Discount(
                amount = "0.00"
            ),
            subscription = ProfileQuery.Subscription(
                amount = "0.00"
            )
        ),
        nextChargeDate = LocalDate.of(2020, 1, 1),
        chargeHistory = emptyList(),
        bankAccount = null,
        activePaymentMethods = null,
        cashback = ProfileQuery.Cashback(
            fragments = ProfileQuery.Cashback.Fragments(
                CashbackFragment(
                    name = "Example Charity",
                    imageUrl = null,
                    paragraph = null
                )
            )
        ),
        cashbackOptions = emptyList(),
        redeemedCampaigns = emptyList(),
        referralInformation = ProfileQuery.ReferralInformation(
            campaign = ProfileQuery.Campaign(
                code = "TEST123",
                incentive = null
            )
        )
    )
}
