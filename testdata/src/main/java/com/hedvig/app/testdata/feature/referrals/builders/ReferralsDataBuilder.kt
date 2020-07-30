package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.testdata.common.builders.CostBuilder

data class ReferralsDataBuilder(
    val insuranceCost: CostFragment = CostBuilder()
        .build(),
    val code: String = "TEST123",
    val incentiveAmount: String = "10.00",
    val incentiveCurrency: String = "SEK",
    val costReducedIndefiniteDiscount: CostFragment = CostBuilder()
        .build(),
    val referredBy: ReferralFragment? = null,
    val invitations: List<ReferralFragment> = emptyList()
) {
    fun build() = ReferralsQuery.Data(
        insuranceCost = ReferralsQuery.InsuranceCost(
            fragments = ReferralsQuery.InsuranceCost.Fragments(
                insuranceCost
            )
        ),
        referralInformation = ReferralsQuery.ReferralInformation(
            campaign = ReferralsQuery.Campaign(
                code = code,
                incentive = ReferralsQuery.Incentive(
                    asMonthlyCostDeduction = ReferralsQuery.AsMonthlyCostDeduction(
                        amount = ReferralsQuery.Amount(
                            fragments = ReferralsQuery.Amount.Fragments(
                                MonetaryAmountFragment(
                                    amount = incentiveAmount,
                                    currency = incentiveCurrency
                                )
                            )
                        )
                    )
                )
            ),
            costReducedIndefiniteDiscount = ReferralsQuery.CostReducedIndefiniteDiscount(
                fragments = ReferralsQuery.CostReducedIndefiniteDiscount.Fragments(
                    costReducedIndefiniteDiscount
                )
            ),
            referredBy = referredBy?.let {
                ReferralsQuery.ReferredBy(
                    fragments = ReferralsQuery.ReferredBy.Fragments(
                        it
                    )
                )
            },
            invitations = invitations.map {
                ReferralsQuery.Invitation(
                    fragments = ReferralsQuery.Invitation.Fragments(
                        it
                    )
                )
            }
        )
    )
}
