package com.hedvig.app.feature.payment

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_NOT_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class PayinNotConnectedTest : TestCase() {

    @get:Rule
    val activityRule = IntentsTestRule(PaymentActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_NOT_CONNECTED) },
        PayinStatusQuery.QUERY_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) }
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
    fun shouldShowConnectPayinWhenPayinIsNotConnected() = run {
        every { marketProvider.market } returns Market.SE

        activityRule.launchActivity(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            trustlyConnectPayin { stub() }
            recycler {
                childAt<PaymentScreen.ConnectPayin>(1) {
                    connect { click() }
                }
            }

            trustlyConnectPayin { intended() }
        }
    }
}
