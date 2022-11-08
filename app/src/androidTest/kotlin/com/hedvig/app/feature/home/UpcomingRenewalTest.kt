package com.hedvig.app.feature.home

import com.hedvig.android.apollo.graphql.HomeQuery
import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_UPCOMING_RENEWAL
import com.hedvig.app.testdata.feature.home.builders.HomeDataBuilder
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.hedvig.app.util.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Rule
import org.junit.Test

class UpcomingRenewalTest : TestCase() {
  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    HomeQuery.OPERATION_DOCUMENT to apolloResponse {
      success(HOME_DATA_UPCOMING_RENEWAL)
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowRenewalWhenUserHasRenewal() = run {
    activityRule.launch(LoggedInActivity.newInstance(context()))
    stubExternalIntents()
    Screen.onScreen<HomeTabScreen> {
      recycler {
        childAt<HomeTabScreen.UpcomingRenewal>(3) {
          title {
            hasText(hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_TITLE, HomeDataBuilder.CONTRACT_DISPLAY_NAME)
          }
          button {
            hasText(hedvig.resources.R.string.DASHBOARD_RENEWAL_PROMPTER_CTA)
            click()
          }
          link { intended() }
        }
      }
    }
  }
}
