package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class CoverageTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowCoverageItems() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0].id
            )
        )

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.CoverageTab>(1) {
                    recycler {
                        hasSize(
                            2 +
                                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
                                    .contracts[0]
                                    .let { it.perils.size + it.insurableLimits.size }
                        )
                        childAt<ContractDetailScreen.CoverageTab.Peril>(3) {
                            click()
                        }
                    }
                }
            }
        }
    }
}
