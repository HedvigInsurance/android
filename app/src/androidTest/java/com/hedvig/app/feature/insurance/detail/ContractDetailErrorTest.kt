package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ContractDetailErrorTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

    var shouldFail = true

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            if (shouldFail) {
                shouldFail = false
                graphQLError("error")
            } else {
                success(
                    INSURANCE_DATA_SWEDISH_HOUSE
                )
            }
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldReload() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0].id
            )
        )

        Screen.onScreen<ContractDetailScreen> {
            retry {
                click()
            }
            tabContent {
                childAt<ContractDetailScreen.YourInfoTab>(0) {
                    recycler {
                        childAt<ContractDetailScreen.YourInfoTab.Header>(0) {
                            text { hasText(R.string.CONTRACT_DETAIL_HOME_TITLE) }
                        }
                    }
                }
            }
        }
    }
}
