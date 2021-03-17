package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class DocumentsTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(ContractDetailActivity::class.java)

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
    fun shouldShowDocuments() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0].id
            )
        )
        stubExternalIntents()

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.DocumentsTab>(2) {
                    recycler {
                        hasSize(2)
                        childAt<ContractDetailScreen.DocumentsTab.Button>(0) {
                            button { click() }
                        }
                    }
                    agreementUrl { intended() }
                }
            }
        }
    }
}
