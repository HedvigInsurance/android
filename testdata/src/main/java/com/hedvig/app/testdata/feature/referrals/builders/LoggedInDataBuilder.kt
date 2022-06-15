package com.hedvig.app.testdata.feature.referrals.builders

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.fragment.MonetaryAmountFragment

data class LoggedInDataBuilder(
    val referralTermsUrl: String = "https://www.example.com",
    val campaignIncentiveAmount: String = "10.00",
    val campaignIncentiveCurrency: String = "SEK",

) {
    fun build() = LoggedInQuery.Data(
        referralTerms = LoggedInQuery.ReferralTerms(
            url = referralTermsUrl
        ),
        referralInformation = LoggedInQuery.ReferralInformation(
            campaign = LoggedInQuery.Campaign(
                incentive = LoggedInQuery.Incentive(
                    __typename = "",
                    asMonthlyCostDeduction = LoggedInQuery.AsMonthlyCostDeduction(
                        __typename = "",
                        amount = LoggedInQuery.Amount(
                            __typename = "",
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
