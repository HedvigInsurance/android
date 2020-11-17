package com.hedvig.app.feature.payment

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_FAILED_PAYMENTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FailedPaymentsTest {

    @get:Rule
    val activityRule = ActivityTestRule(PaymentActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_FAILED_PAYMENTS) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowWarningWhenUserHasMissedPayments() {
        activityRule.launchActivity(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.FailedPayments>(0) {
                    paragraph {
                        hasText(
                            R.string.PAYMENTS_LATE_PAYMENTS_MESSAGE,
                            PAYMENT_DATA_FAILED_PAYMENTS.balance.failedCharges!!,
                            PAYMENT_DATA_FAILED_PAYMENTS.nextChargeDate!!
                        )
                    }
                }
            }
        }
    }
}
