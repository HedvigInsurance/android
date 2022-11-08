package com.hedvig.app.feature.home.howclaimswork

import com.hedvig.android.apollo.graphql.HomeQuery
import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.home.screens.HowClaimsWorkScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class HowClaimsWorkTest : TestCase() {
  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    HomeQuery.OPERATION_DOCUMENT to apolloResponse {
      success(HOME_DATA_ACTIVE)
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldOpenClaimFromHowClaimsWork() = run {
    activityRule.launch(LoggedInActivity.newInstance(context()))
    onScreen<HomeTabScreen> {
      recycler {
        childAt<HomeTabScreen.HowClaimsWork>(2) {
          button {
            hasText(hedvig.resources.R.string.home_tab_claim_explainer_button)
            click()
          }
        }
      }
    }
    onScreen<HowClaimsWorkScreen> {
      button {
        hasText(hedvig.resources.R.string.claims_explainer_button_next)
        click()
        click()
        hasText(hedvig.resources.R.string.general_close_button)
      }
    }
  }
}
