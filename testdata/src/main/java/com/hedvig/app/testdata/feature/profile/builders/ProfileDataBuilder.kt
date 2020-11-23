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
   )
}
