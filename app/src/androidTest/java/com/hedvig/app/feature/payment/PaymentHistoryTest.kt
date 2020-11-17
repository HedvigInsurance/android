package com.hedvig.app.feature.payment

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_HISTORIC_PAYMENTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentHistoryTest {

    @get:Rule
    val activityRule = ActivityTestRule(PaymentActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_HISTORIC_PAYMENTS) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowPaymentHistoryWhenUserHasHistoricPayments() {
        activityRule.launchActivity(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.Charge>(2) {
                    amount {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS.chargeHistory[0].amount.fragments.monetaryAmountFragment.toMonetaryAmount()
                                .format(context())
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
                childAt<PaymentScreen.Charge>(3) {
                    amount {
                        hasText(
                            PAYMENT_DATA_HISTORIC_PAYMENTS.chargeHistory[1].amount.fragments.monetaryAmountFragment.toMonetaryAmount()
                                .format(context())
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
            }
        }
    }
}
