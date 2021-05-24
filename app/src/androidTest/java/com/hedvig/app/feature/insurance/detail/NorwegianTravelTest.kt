package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_TRAVEL
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NorwegianTravelTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse { success(INSURANCE_DATA_NORWEGIAN_TRAVEL) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowCoinsuredForNorwegianTravelContract() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_NORWEGIAN_TRAVEL.contracts[0].id
            )
        )

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.YourInfoTab>(0) {
                    recycler {
                        childAt<ContractDetailScreen.YourInfoTab.CoInsured>(0) {
                            title { hasText(R.string.CONTRACT_DETAIL_COINSURED_TITLE) }
                            coInsuredLabel { hasText(R.string.CONTRACT_DETAIL_COINSURED_TITLE) }
                            coInsured { hasText(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, 2) }
                        }
                    }
                }
            }
        }
    }
}
