package com.hedvig.app.feature.payment

import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdyenConnectedTest {

    @get:Rule
    val activityRule = IntentsTestRule(PaymentActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_ADYEN_CONNECTED) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowCardInformationWhenAdyenIsConnected() {
        activityRule.launchActivity(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            recycler {
                childAt<PaymentScreen.AdyenPayinDetails>(1) {
                    cardType { hasText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.brand!!) }
                    maskedCardNumber { containsText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits) }
                    validUntil {
                        containsText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryMonth)
                        containsText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.expiryYear)
                    }
                }
            }
        }
    }
}
