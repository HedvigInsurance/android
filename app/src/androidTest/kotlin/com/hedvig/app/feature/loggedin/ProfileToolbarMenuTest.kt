package com.hedvig.app.feature.loggedin

import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.android.apollo.graphql.TriggerFreeTextChatMutation
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class ProfileToolbarMenuTest : TestCase() {
  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    TriggerFreeTextChatMutation.OPERATION_DOCUMENT to apolloResponse {
      success(TriggerFreeTextChatMutation.Data(true))
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldOpenChatWhenClickingToolbarActionOnProfileTab() = run {
    activityRule.launch(LoggedInActivity.newInstance(context()))

    onScreen<LoggedInScreen> {
      chat { stub() }
      root { isVisible() }
      bottomTabs { setSelectedItem(R.id.profile) }
      openChat {
        isVisible()
        click()
      }
      chat { intended() }
    }
  }
}
