package com.hedvig.app.feature.payment

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class AdyenConnectedTest : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_ADYEN_CONNECTED) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val marketProvider = mockk<MarketProvider>(relaxed = true)

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(marketProviderModule),
        listOf(module { single { marketProvider } })
    )

    @Test
    fun shouldShowCardInformationWhenAdyenIsConnected() = run {
        every { marketProvider.market } returns Market.NO
        activityRule.launch(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            adyenConnectPayin { stub() }
            recycler {
                childAt<PaymentScreen.AdyenPayinDetails>(1) {
                    cardType { hasText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.brand!!) }
                    maskedCardNumber { containsText(PAYMENT_DATA_ADYEN_CONNECTED.activePaymentMethods!!.fragments.activePaymentMethodsFragment.storedPaymentMethodsDetails.lastFourDigits) }
                }
                childAt<PaymentScreen.Link>(2) {
                    button {
                        hasText(R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON)
                        click()
                    }
                }
            }
            adyenConnectPayin { intended() }
        }
    }
}
