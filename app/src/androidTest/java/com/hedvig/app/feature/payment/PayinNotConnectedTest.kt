package com.hedvig.app.feature.payment

import com.hedvig.android.market.Market
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.android.owldroid.graphql.PaymentQuery
import com.hedvig.app.feature.profile.ui.payment.PaymentActivity
import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_NEEDS_SETUP
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_NOT_CONNECTED
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

class PayinNotConnectedTest : TestCase() {

  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(PaymentActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    PaymentQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYMENT_DATA_NOT_CONNECTED) },
    PayinStatusQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_NEEDS_SETUP) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val marketRule = MarketRule(Market.SE)

  @get:Rule
  val featureFlagRule = FeatureFlagRule(
    paymentType = PaymentType.TRUSTLY,
  )

  @Test
  fun shouldShowConnectPayinWhenPayinIsNotConnected() = run {
    activityRule.launch(PaymentActivity.newInstance(context()))

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
