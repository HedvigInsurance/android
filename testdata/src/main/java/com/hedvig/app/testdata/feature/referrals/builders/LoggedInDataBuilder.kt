package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.LoggedInQuery

data class LoggedInDataBuilder(
    val referralTermsUrl: String = "https://www.example.com",
    val campaignIncentiveAmount: String = "10.00",
    val campaignIncentiveCurrency: String = "SEK"

) {
    fun build() = LoggedInQuery.Data(
        referralTerms = LoggedInQuery.ReferralTerms(
            url = referralTermsUrl
        ),
        referralInformation = LoggedInQuery.ReferralInformation(
            campaign = LoggedInQuery.Campaign(
                incentive = LoggedInQuery.Incentive(
                    asMonthlyCostDeduction = LoggedInQuery.AsMonthlyCostDeduction(
                        amount = LoggedInQuery.Amount(
                            fragments = LoggedInQuery.Amount.Fragments(
                                MonetaryAmountFragment(
                                    amount = campaignIncentiveAmount,
                                    currency = campaignIncentiveCurrency
                                )
                            )
                        )
                    )
                )
            )
        )
    )
}
