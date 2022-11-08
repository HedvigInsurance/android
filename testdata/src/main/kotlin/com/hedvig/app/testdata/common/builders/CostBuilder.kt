package com.hedvig.app.testdata.common.builders

import com.hedvig.android.apollo.graphql.fragment.CostFragment
import com.hedvig.android.apollo.graphql.fragment.MonetaryAmountFragment

data class CostBuilder(
  val currency: String = "SEK",
  val discountAmount: String = "0.00",
  val netAmount: String = "349.00",
  val grossAmount: String = "349.00",
) {
  fun build() = CostFragment(
    monthlyDiscount = CostFragment.MonthlyDiscount(
      __typename = "",
      fragments = CostFragment.MonthlyDiscount.Fragments(
        MonetaryAmountFragment(
          amount = discountAmount,
          currency = currency,
        ),
      ),
    ),
    monthlyNet = CostFragment.MonthlyNet(
      __typename = "",
      fragments = CostFragment.MonthlyNet.Fragments(
        MonetaryAmountFragment(
          amount = netAmount,
          currency = currency,
        ),
      ),
    ),
    monthlyGross = CostFragment.MonthlyGross(
      __typename = "",
      fragments = CostFragment.MonthlyGross.Fragments(
        MonetaryAmountFragment(
          amount = grossAmount,
          currency = currency,
        ),
      ),
    ),
  )
}
