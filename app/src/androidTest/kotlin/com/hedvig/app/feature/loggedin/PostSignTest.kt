package com.hedvig.app.feature.loggedin

import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.android.apollo.graphql.WelcomeQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity.Companion.EXTRA_IS_FROM_ONBOARDING
import com.hedvig.app.testdata.feature.loggedin.WELCOME_DATA_ONE_PAGE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class PostSignTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    WelcomeQuery.OPERATION_DOCUMENT to apolloResponse {
      success(WELCOME_DATA_ONE_PAGE)
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldOpenWelcomeWhenNavigatingFromOnboarding() = run {
    activityRule.launch(
      LoggedInActivity.newInstance(context())
        .apply { putExtra(EXTRA_IS_FROM_ONBOARDING, true) },
    )

    onScreen<WelcomeScreen> {
      pressBack()
    }
    onScreen<LoggedInScreen> {
      root { isVisible() }
      bottomTabs {
        hasSelectedItem(R.id.home)
      }
    }
  }
}
