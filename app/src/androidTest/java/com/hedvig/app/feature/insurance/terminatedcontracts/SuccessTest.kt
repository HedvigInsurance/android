package com.hedvig.app.feature.insurance.terminatedcontracts

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.screens.InsuranceScreen
import com.hedvig.app.feature.insurance.screens.TerminatedContractsScreen
import com.hedvig.app.feature.insurance.ui.terminatedcontracts.TerminatedContractsActivity
import com.hedvig.app.testdata.dashboard.INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SuccessTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(TerminatedContractsActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(INSURANCE_DATA_ONE_ACTIVE_ONE_TERMINATED)
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

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
