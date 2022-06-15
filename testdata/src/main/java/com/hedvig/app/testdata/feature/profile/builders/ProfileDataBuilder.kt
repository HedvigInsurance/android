package com.hedvig.app.testdata.feature.profile.builders

import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.android.owldroid.graphql.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.owldroid.graphql.fragment.CashbackFragment
import com.hedvig.android.owldroid.graphql.fragment.CostFragment
import com.hedvig.android.owldroid.graphql.type.DirectDebitStatus
import com.hedvig.app.testdata.common.builders.CostBuilder
import java.util.UUID

data class ProfileDataBuilder(
    private val memberId: String = "123",
    private val firstName: String = "Test",
    private val lastName: String? = "Testerson",
    private val email: String? = "test@example.com",
    private val phoneNumber: String? = "07012345678",
    private val directDebitStatus: DirectDebitStatus = DirectDebitStatus.NEEDS_SETUP,
    private val adyenConnected: Boolean = false,
    private val cost: CostFragment = CostBuilder().build(),
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
            __typename = "",
            freeUntil = null,
            fragments = ProfileQuery.InsuranceCost.Fragments(
                costFragment = cost
            )
        ),
        cashback = ProfileQuery.Cashback(
            __typename = "",
            fragments = ProfileQuery.Cashback.Fragments(
                CashbackFragment(
                    id = UUID.randomUUID().toString(),
                    name = "Example Charity",
                    imageUrl = null,
                    description = null
                )
            )
        ),
        cashbackOptions = emptyList(),
        bankAccount = ProfileQuery.BankAccount(directDebitStatus = directDebitStatus),
        activePaymentMethodsV2 = if (adyenConnected) {
            ProfileQuery.ActivePaymentMethodsV2(
                __typename = "",
                fragments = ProfileQuery.ActivePaymentMethodsV2.Fragments(
                    activePaymentMethodsFragment = ActivePaymentMethodsFragment(
                        __typename = "",
                        asStoredCardDetails = ActivePaymentMethodsFragment.AsStoredCardDetails(
                            __typename = "",
                            brand = "test",
                            lastFourDigits = "1234",
                            expiryMonth = "1",
                            expiryYear = "2022"
                        ),
                        asStoredThirdPartyDetails = null
                    )
                )
            )
        } else {
            null
        },
    )
}
