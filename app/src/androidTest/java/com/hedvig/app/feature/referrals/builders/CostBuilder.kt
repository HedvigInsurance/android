package com.hedvig.app.feature.referrals.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment

data class CostBuilder(
    val currency: String = "SEK",
    val discountAmount: String = "0.00",
    val netAmount: String = "349.00",
    val grossAmount: String = "349.00"
) {
    fun build() = CostFragment(
        monthlyDiscount = CostFragment.MonthlyDiscount(
            fragments = CostFragment.MonthlyDiscount.Fragments(
                MonetaryAmountFragment(
                    amount = discountAmount,
                    currency = currency
                )
            )
        ),
        monthlyNet = CostFragment.MonthlyNet(
            fragments = CostFragment.MonthlyNet.Fragments(
                MonetaryAmountFragment(
                    amount = netAmount,
                    currency = currency
                )
            )
        ),
        monthlyGross = CostFragment.MonthlyGross(
            fragments = CostFragment.MonthlyGross.Fragments(
                MonetaryAmountFragment(
                    amount = grossAmount,
                    currency = currency
                )
            )
        )
    )
}
