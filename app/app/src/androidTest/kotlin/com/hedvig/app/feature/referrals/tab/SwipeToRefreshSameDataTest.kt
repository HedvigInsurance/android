package com.hedvig.app.feature.referrals.tab

import com.hedvig.android.hanalytics.featureflags.flags.Feature
import com.hedvig.android.navigation.core.TopLevelGraph
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.FeatureFlagRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import giraffe.LoggedInQuery
import giraffe.ReferralsQuery
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Rule
import org.junit.Test

class SwipeToRefreshSameDataTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    ReferralsQuery.OPERATION_DOCUMENT to apolloResponse { success(REFERRALS_DATA_WITH_NO_DISCOUNTS) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @get:Rule
  val featureFlagRule = FeatureFlagRule(
    Feature.REFERRAL_CAMPAIGN to false,
    Feature.FOREVER to true,
  )

  @Test
  fun shouldRefreshDataWhenSwipingDownToRefreshWhenDataHasNotChanged() = run {
    val intent = LoggedInActivity.newInstance(
      context(),
      initialTab = TopLevelGraph.FOREVER,
    )

    activityRule.launch(intent)

    Screen.onScreen<ReferralTabScreen> {
      share { isVisible() }
      recycler {
        hasSize(3)
      }
      swipeToRefresh { swipeDown() }
      recycler { hasSize(3) }
      swipeToRefresh { isNotRefreshing() }
    }
  }
}
