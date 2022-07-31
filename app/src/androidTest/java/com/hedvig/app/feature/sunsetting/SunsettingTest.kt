package com.hedvig.app.feature.sunsetting

import com.hedvig.android.core.common.jsonObjectOf
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apollo.SunsettingInterceptor
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.screens.KScreen
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.intent.KIntent
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

class SunsettingTest : TestCase() {
  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      graphQLError(
        jsonObjectOf(
          "message" to "Outdated app",
          "errorMessage" to "Buildversion -1 triggers test case for invalid versions",
          "errorCode" to SunsettingInterceptor.SUNSETTING_ERROR_CODE,
          "supportPhoneNumber" to "+4670 123 45 67",
          "supportEmail" to "info@hedvig.com",
        ),
      )
    },
    HomeQuery.OPERATION_DOCUMENT to apolloResponse { success(HOME_DATA_PENDING) },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  @Ignore("Racing condition with sunsetting on apollo SunsettingInterceptor which is running on a coroutine")
  fun whenReceivingResponseWithSunsetErrorShouldOpenForceUpgrade() = run {
    ForceUpgradeScreen {
      step("Stub force upgrade") {
        intent { stub() }
      }
      step("Launch activity which will receive sunsetting-error") {
        activityRule.launch()
      }
      step("Verify that force upgrade was launched") {
        intent { intended() }
      }
    }
  }
}

object ForceUpgradeScreen : KScreen<ForceUpgradeScreen>() {
  override val layoutId: Int? = null
  override val viewClass = ForceUpgradeActivity::class.java

  val intent = KIntent {
    hasComponent(ForceUpgradeActivity::class.java.name)
  }
}
