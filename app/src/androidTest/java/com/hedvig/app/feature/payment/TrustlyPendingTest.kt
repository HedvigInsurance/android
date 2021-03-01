package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_PENDING
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_TRUSTLY_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TrustlyPendingTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_TRUSTLY_CONNECTED) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_PENDING) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.SE)
    @Test
    fun shouldShowPendingStatusWhenUserHasRecentlyChangedBankAccount() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            trustlyConnectPayin { stub() }
            recycler {
                childAt<PaymentScreen.TrustlyPayinDetails>(3) {
                    bank { hasText(R.string.PAYMENTS_DIRECT_DEBIT_PENDING) }
                    accountNumber {
                        containsText(
                            PAYMENT_DATA_TRUSTLY_CONNECTED.bankAccount!!.fragments.bankAccountFragment.descriptor
                        )
                    }
                    pending { isVisible() }
                }
                childAt<PaymentScreen.Link>(4) {
                    button {
                        hasText(R.string.PROFILE_PAYMENT_CHANGE_BANK_ACCOUNT)
                        click()
                    }
                }
            }
            trustlyConnectPayin { intended() }
        }
    }
}
