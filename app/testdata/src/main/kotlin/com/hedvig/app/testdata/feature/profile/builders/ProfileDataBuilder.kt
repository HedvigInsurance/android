package com.hedvig.app.testdata.feature.profile.builders

import com.hedvig.app.testdata.common.builders.CostBuilder
import giraffe.ProfileQuery
import giraffe.fragment.ActivePaymentMethodsFragment
import giraffe.fragment.CostFragment
import giraffe.fragment.MonetaryAmountFragment
import giraffe.type.DirectDebitStatus
import giraffe.type.StoredCardDetails

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
    chargeEstimation = ProfileQuery.ChargeEstimation(
      subscription = ProfileQuery.Subscription(
        __typename = "",
        fragments = ProfileQuery.Subscription.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("100", "SEK"),
        ),
      ),
      discount = ProfileQuery.Discount(
        __typename = "",
        fragments = ProfileQuery.Discount.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("100", "SEK"),
        ),
      ),
      charge = ProfileQuery.Charge(
        __typename = "",
        fragments = ProfileQuery.Charge.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("10", "SEK"),
        ),
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
