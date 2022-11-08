package com.hedvig.app.feature.loggedin

import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasNumberOfMenuItems
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class NavBarReferralsTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
  )

  @get:Rule
  val featureFlagRule = FeatureFlagRule(
    Feature.REFERRALS to true,
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowAllIcons() = run {
    activityRule.launch(LoggedInActivity.newInstance(context()))

    onScreen<LoggedInScreen> {
      bottomTabs {
        hasNumberOfMenuItems(4)
      }
    }
  }
}
