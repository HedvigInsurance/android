package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_FAILED_PAYMENTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class FailedPaymentsTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_FAILED_PAYMENTS) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowWarningWhenUserHasMissedPayments() = run {
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.FailedPayments>(1) {
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
