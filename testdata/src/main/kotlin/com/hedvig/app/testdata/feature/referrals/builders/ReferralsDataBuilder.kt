package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.app.testdata.common.builders.CostBuilder
import giraffe.ReferralsQuery
import giraffe.fragment.CostFragment
import giraffe.fragment.MonetaryAmountFragment
import giraffe.fragment.ReferralFragment
import giraffe.type.MonthlyCostDeduction

data class ReferralsDataBuilder(
  val insuranceCost: CostFragment = CostBuilder()
    .build(),
  val code: String = "TEST123",
  val incentiveAmount: String = "10.00",
  val incentiveCurrency: String = "SEK",
  val costReducedIndefiniteDiscount: CostFragment = CostBuilder()
    .build(),
  val referredBy: ReferralFragment? = null,
  val invitations: List<ReferralFragment> = emptyList(),
) {
  fun build() = ReferralsQuery.Data(
    chargeEstimation = ReferralsQuery.ChargeEstimation(
      subscription = ReferralsQuery.Subscription(
        __typename = "",
        fragments = ReferralsQuery.Subscription.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("100", "SEK"),
        ),
      ),
      discount = ReferralsQuery.Discount(
        __typename = "",
        fragments = ReferralsQuery.Discount.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("100", "SEK"),
        ),
      ),
      charge = ReferralsQuery.Charge(
        __typename = "",
        fragments = ReferralsQuery.Charge.Fragments(
          monetaryAmountFragment = MonetaryAmountFragment("10", "SEK"),
        ),
      ),
    ),
    referralInformation = ReferralsQuery.ReferralInformation(
      campaign = ReferralsQuery.Campaign(
        code = code,
        incentive = ReferralsQuery.Incentive(
          __typename = MonthlyCostDeduction.type.name,
          asMonthlyCostDeduction = ReferralsQuery.AsMonthlyCostDeduction(
            __typename = MonthlyCostDeduction.type.name,
            amount = ReferralsQuery.Amount(
              __typename = "",
              fragments = ReferralsQuery.Amount.Fragments(
                MonetaryAmountFragment(
                  amount = incentiveAmount,
                  currency = incentiveCurrency,
                ),
              ),
            ),
          ),
        ),
      ),
      costReducedIndefiniteDiscount = ReferralsQuery.CostReducedIndefiniteDiscount(
        __typename = "",
        fragments = ReferralsQuery.CostReducedIndefiniteDiscount.Fragments(
          costReducedIndefiniteDiscount,
        ),
      ),
      referredBy = referredBy?.let {
        ReferralsQuery.ReferredBy(
          __typename = it.__typename,
          fragments = ReferralsQuery.ReferredBy.Fragments(
            it,
          ),
        )
      },
      invitations = invitations.map {
        ReferralsQuery.Invitation(
          __typename = it.__typename,
          fragments = ReferralsQuery.Invitation.Fragments(
            it,
          ),
        )
      },
    ),
  )
}
