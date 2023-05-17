package com.hedvig.app.feature.changeaddress

import com.hedvig.app.feature.home.ui.changeaddress.ChangeAddressActivity
import com.hedvig.app.testdata.feature.changeaddress.SELF_CHANGE_ELIGIBILITY
import com.hedvig.app.testdata.feature.changeaddress.UPCOMING_AGREEMENT_SWEDISH_APARTMENT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.ActiveContractBundlesQuery
import giraffe.UpcomingAgreementQuery
import org.junit.Rule
import org.junit.Test

class UpcomingChangeAddressTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(ChangeAddressActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    UpcomingAgreementQuery.OPERATION_DOCUMENT to apolloResponse { success(UPCOMING_AGREEMENT_SWEDISH_APARTMENT) },
    ActiveContractBundlesQuery.OPERATION_DOCUMENT to apolloResponse { success(SELF_CHANGE_ELIGIBILITY) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowManualChangeAddressWhenEligibilityIsBlocked() = run {
    activityRule.launch(ChangeAddressActivity.newInstance(context()))

    ChangeAddressScreen {
      title {
        hasText(hedvig.resources.R.string.moving_intro_existing_move_title)
      }

      subtitle {
        hasText(hedvig.resources.R.string.moving_intro_existing_move_description)
      }

      continueButton {
        hasText(hedvig.resources.R.string.moving_intro_manual_handling_button_text)
      }
    }
  }
}
