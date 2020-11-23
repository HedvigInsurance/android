package com.hedvig.app.feature.payment

import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.R
import com.hedvig.app.feature.marketpicker.Market
import com.hedvig.app.feature.marketpicker.MarketProvider
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.marketProviderModule
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_TRUSTLY_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class TrustlyConnectedTest {

    @get:Rule
    val activityRule = IntentsTestRule(PaymentActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        PaymentQuery.QUERY_DOCUMENT to apolloResponse { success(PAYMENT_DATA_TRUSTLY_CONNECTED) },
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
    fun shouldShowBankAccountInformationWhenTrustlyIsConnected() {
        every { marketProvider.market } returns Market.SE
        activityRule.launchActivity(PaymentActivity.newInstance(context()))

        onScreen<PaymentScreen> {
            trustlyConnectPayin { stub() }
            recycler {
                childAt<PaymentScreen.TrustlyPayinDetails>(1) {
                    accountNumber {
                        containsText(PAYMENT_DATA_TRUSTLY_CONNECTED.bankAccount!!.fragments.bankAccountFragment.bankName)
                        containsText(PAYMENT_DATA_TRUSTLY_CONNECTED.bankAccount!!.fragments.bankAccountFragment.descriptor)
                    }
                    status { hasText(R.string.PAYMENTS_DIRECT_DEBIT_ACTIVE) }
                    pending { isGone() }
                }
                childAt<PaymentScreen.Link>(2) {
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
