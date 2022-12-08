package com.hedvig.app.feature.splash

import com.hedvig.android.apollo.graphql.ContractStatusQuery
import com.hedvig.app.MainActivity
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NetworkErrorTest : TestCase() {
  @get:Rule
  val activityRule = LazyIntentsActivityScenarioRule(MainActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    ContractStatusQuery.OPERATION_DOCUMENT to apolloResponse { internalServerError() },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldNotCrashOnNetworkError() = run {
    activityRule.launch()
  }
}
