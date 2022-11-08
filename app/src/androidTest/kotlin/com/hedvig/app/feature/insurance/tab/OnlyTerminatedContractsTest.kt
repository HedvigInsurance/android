package com.hedvig.app.feature.insurance.tab

import com.hedvig.android.apollo.graphql.InsuranceQuery
import com.hedvig.android.apollo.graphql.LoggedInQuery
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_TERMINATED
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

class OnlyTerminatedContractsTest : TestCase() {

  @get:Rule
  val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

  @get:Rule
  val mockServerRule = ApolloMockServerRule(
    LoggedInQuery.OPERATION_DOCUMENT to apolloResponse {
      success(LOGGED_IN_DATA)
    },
    InsuranceQuery.OPERATION_DOCUMENT to apolloResponse {
      success(INSURANCE_DATA_TERMINATED)
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldShowTerminatedContractsOnTabWhenUserHasOnlyTerminatedContracts() = run {
    val intent = LoggedInActivity.newInstance(
      context(),
      initialTab = LoggedInTabs.INSURANCE,
    )
    activityRule.launch(intent)

    onScreen<InsuranceScreen> {
      insuranceRecycler {
        hasSize(2)
        childAt<InsuranceScreen.ContractCard>(1) {
          contractName { isVisible() }
        }
      }
    }
  }
}
