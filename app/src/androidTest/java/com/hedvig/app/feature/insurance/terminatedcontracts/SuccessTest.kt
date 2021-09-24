package com.hedvig.app.feature.insurance.terminatedcontracts

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.insurance.screens.TerminatedContractsScreen
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class SuccessTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(TerminatedContractsActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowOneTerminatedContractWhenUserHasOneTerminatedContract() = run {
        activityRule.launch(TerminatedContractsActivity.newInstance(context()))

        onScreen<TerminatedContractsScreen> {
            recycler {
                childAt<InsuranceScreen.ContractCard>(0) {
                    contractName { isVisible() }
                }
            }
        }
    }
}
