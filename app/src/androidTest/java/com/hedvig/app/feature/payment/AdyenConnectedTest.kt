package com.hedvig.app.feature.payment

import com.hedvig.android.market.Market
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_ADYEN_CONNECTED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.hedvig.hanalytics.PaymentType
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class AdyenConnectedTest : TestCase() {

  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    PaymentQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYMENT_DATA_ADYEN_CONNECTED) },
    PayinStatusQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val marketRule = MarketRule(Market.NO)

  @get:Rule
  val featureFlagRule = FeatureFlagRule(paymentType = PaymentType.ADYEN)

  @Test
  fun shouldShowCardInformationWhenAdyenIsConnected() = run {
    activityRule.launch(PaymentActivity.newInstance(context()))

    onScreen<PaymentScreen> {
      adyenConnectPayin { stub() }
      recycler {
        childAt<PaymentScreen.AdyenPayinDetails>(3) {
          cardType {
            hasText(
              PAYMENT_DATA_ADYEN_CONNECTED
                .activePaymentMethodsV2!!
                .fragments
                .activePaymentMethodsFragment
                .asStoredCardDetails!!.brand!!,
            )
          }
          maskedCardNumber {
            containsText(
              PAYMENT_DATA_ADYEN_CONNECTED
                .activePaymentMethodsV2!!
                .fragments
                .activePaymentMethodsFragment
                .asStoredCardDetails!!.lastFourDigits,
            )
          }
        }
        childAt<PaymentScreen.Link>(4) {
          button {
            hasText(hedvig.resources.R.string.MY_PAYMENT_CHANGE_CREDIT_CARD_BUTTON)
            click()
          }
        }
      }
      adyenConnectPayin { intended() }
    }
  }
}
