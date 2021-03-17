package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_PAYOUT_PENDING
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class AdyenPayoutPendingTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_PAYOUT_PENDING) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.NO)

    @Test
    fun shouldShowConnectPayoutWhenInNorwayAndPayoutIsPending() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            adyenConnectPayout { stub() }
            recycler {
                childAt<PaymentScreen.AdyenPayoutDetails>(4) {
                    status {
                        hasText(R.string.payment_screen_bank_account_processing)
                    }
                }
                childAt<PaymentScreen.AdyenPayoutParagraph>(5) {
                    text { hasText(R.string.payment_screen_pay_out_footer_pending) }
                }
                childAt<PaymentScreen.Link>(6) {
                    click()
                }
            }
            adyenConnectPayout { intended() }
        }
    }
}
