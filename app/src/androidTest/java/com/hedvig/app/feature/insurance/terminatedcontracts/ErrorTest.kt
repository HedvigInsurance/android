package com.hedvig.app.feature.insurance.terminatedcontracts

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.insurance.screens.TerminatedContractsScreen
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class ErrorTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(TerminatedContractsActivity::class.java)

    var shouldFail = true

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError(jsonObjectOf("message" to "error"))
            } else {
                success(INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED)
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowErrorOnGraphQLError() = run {
        activityRule.launch(TerminatedContractsActivity.newInstance(context()))

        onScreen<TerminatedContractsScreen> {
            recycler {
                childAt<InsuranceScreen.Error>(0) {
                    retry { click() }
                }
                childAt<InsuranceScreen.ContractCard>(0) {
                    contractName { isVisible() }
                }
            }
        }
    }
}
