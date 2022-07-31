package com.hedvig.app.feature.embark

import com.hedvig.android.core.common.jsonObjectOf
import com.hedvig.android.market.Market
import com.hedvig.android.owldroid.graphql.MemberIdQuery
import com.hedvig.app.feature.embark.screens.MoreOptionsScreen
import com.hedvig.app.feature.embark.ui.MoreOptionsActivity
import com.hedvig.app.testdata.feature.onboarding.MEMBER_ID_DATA
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class MoreOptionsTest : TestCase() {

  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(MoreOptionsActivity::class.java)

  var shouldFail = true

  @get:Rule
  val marketRule = MarketRule(Market.NO)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    MemberIdQuery.OPERATION_DOCUMENT to apolloResponse {
      if (shouldFail) {
        shouldFail = false
        graphQLError(jsonObjectOf("message" to "error"))
      } else {
        success(MEMBER_ID_DATA)
      }
    },
  )

  @Test
  fun openMoreOptionsActivity() = run {
    activityRule.launch(MoreOptionsActivity.newInstance(context()))
    MoreOptionsScreen {
      recycler {
        childAt<MoreOptionsScreen.Row>(1) {
          info {
            click()
            hasText("1234567890")
          }
        }
      }
    }
  }
}
