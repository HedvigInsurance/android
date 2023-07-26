package com.hedvig.app.feature.payment

import com.hedvig.app.testdata.feature.payment.PAYIN_STATUS_DATA_ACTIVE
import com.hedvig.app.testdata.feature.payment.PAYMENT_DATA_REFERRAL
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.locale
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.PayinStatusQuery
import giraffe.PaymentQuery
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class ReferralCampaignTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(PaymentActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    PaymentQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYMENT_DATA_REFERRAL) },
    PayinStatusQuery.OPERATION_DOCUMENT to apolloResponse { success(PAYIN_STATUS_DATA_ACTIVE) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowReferralDiscount() = run {
    activityRule.launch(PaymentActivity.newInstance(context()))

    onScreen<PaymentScreen> {
      recycler {
        childAt<PaymentScreen.NextPayment>(1) {
          gross {
            isVisible()
            hasText(
              PAYMENT_DATA_REFERRAL
                .insuranceCost!!
                .fragments
                .costFragment
                .monthlyGross
                .fragments
                .monetaryAmountFragment
                .toMonetaryAmount()
                .format(locale()),
            )
          }
          net {
            hasText(
              PAYMENT_DATA_REFERRAL
                .chargeEstimation
                .charge
                .fragments
                .monetaryAmountFragment
                .toMonetaryAmount()
                .format(locale()),
            )
          }
        }
        childAt<PaymentScreen.Campaign>(2) {
          isVisible()
        }
      }
    }
  }
}
