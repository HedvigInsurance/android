package com.hedvig.app.feature.payment

import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_HISTORIC_PAYMENTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class PaymentHistoryTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_HISTORIC_PAYMENTS) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowPaymentHistoryWhenUserHasHistoricPayments() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            paymentHistory { stub() }
            recycler {
                childAt<PaymentScreen.Charge>(3) {
                    amount {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS
                                .chargeHistory[0]
                                .amount
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()
                                .format(context(), market())
                        )
                    }
                    date {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS.chargeHistory[0].date.format(
                                PaymentActivity.DATE_FORMAT
                            )
                        )
                    }
                }
                childAt<PaymentScreen.Charge>(4) {
                    amount {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS
                                .chargeHistory[1]
                                .amount
                                .fragments
                                .monetaryAmountFragment
                                .toMonetaryAmount()
                                .format(context(), market())
                        )
                    }
                    date {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS.chargeHistory[1].date.format(
                                PaymentActivity.DATE_FORMAT
                            )
                        )
                    }
                }
                childAt<PaymentScreen.PaymentHistoryLink>(5) {
                    click()
                }
            }
            paymentHistory { intended() }
        }
    }
}
