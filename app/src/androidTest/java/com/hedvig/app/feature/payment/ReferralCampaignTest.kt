package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_REFERRAL
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ReferralCampaignTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_REFERRAL) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowReferralDiscount() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.NextPayment>(1) {
                    gross {
                        isVisible()
                        hasText(
                            PAYMENT_DATA_REFERRAL
                                .insuranceCost!!
                                .fragments
                                .costFragment
                                .monthlyGross
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()
                                .format(context(), market())
                        )
                    }
                    net {
                        hasText(
                            PAYMENT_DATA_REFERRAL
                                .chargeEstimation
                                .charge
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()
                                .format(context(), market())
                        )
                    }
                }
                childAt<PaymentScreen.Campaign>(2) {
                    isVisible()
                }
            }
        }
    }
}
