package com.hedvig.app.feature.insurance.detail

import androidx.test.espresso.intent.rule.IntentsTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stubExternalIntents
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class DocumentsTest : TestCase() {
    @get:Rule
    val activityRule = IntentsTestRule(ContractDetailActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowDocuments() = run {
        activityRule.launchActivity(
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
