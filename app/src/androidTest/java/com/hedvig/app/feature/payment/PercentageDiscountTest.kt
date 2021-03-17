package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PERCENTAGE_CAMPAIGN
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class PercentageDiscountTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_PERCENTAGE_CAMPAIGN) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowPercentageDiscount() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.NextPayment>(1) {
                    discount {
                        isVisible()
                        containsText(
                            PAYMENT_DATA_PERCENTAGE_CAMPAIGN
                                .redeemedCampaigns[0]
                                .fragments
                                .incentiveFragment
                                .incentive!!
                                .asPercentageDiscountMonths!!
                                .percentageDiscount
                                .toInt()
                                .toString()
                        )
                    }
                }
            }
        }
    }
}
