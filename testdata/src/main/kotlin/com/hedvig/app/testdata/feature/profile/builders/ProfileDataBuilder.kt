package com.hedvig.app.testdata.feature.profile.builders

import com.hedvig.android.apollo.graphql.ProfileQuery
import com.hedvig.android.apollo.graphql.fragment.ActivePaymentMethodsFragment
import com.hedvig.android.apollo.graphql.fragment.CostFragment
import com.hedvig.android.apollo.graphql.type.DirectDebitStatus
import com.hedvig.android.apollo.graphql.type.StoredCardDetails
import com.hedvig.app.testdata.common.builders.CostBuilder

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
      phoneNumber = phoneNumber,
    ),
    insuranceCost = ProfileQuery.InsuranceCost(
      __typename = "",
      freeUntil = null,
      fragments = ProfileQuery.InsuranceCost.Fragments(
        costFragment = cost,
      ),
    ),
    bankAccount = ProfileQuery.BankAccount(directDebitStatus = directDebitStatus),
    activePaymentMethodsV2 = if (adyenConnected) {
      ProfileQuery.ActivePaymentMethodsV2(
        __typename = "",
        fragments = ProfileQuery.ActivePaymentMethodsV2.Fragments(
          activePaymentMethodsFragment = ActivePaymentMethodsFragment(
            __typename = StoredCardDetails.type.name,
            asStoredCardDetails = ActivePaymentMethodsFragment.AsStoredCardDetails(
              __typename = StoredCardDetails.type.name,
              brand = "test",
              lastFourDigits = "1234",
              expiryMonth = "1",
              expiryYear = "2022",
            ),
            asStoredThirdPartyDetails = null,
          ),
        ),
      )
    } else {
      null
    },
  )
}
