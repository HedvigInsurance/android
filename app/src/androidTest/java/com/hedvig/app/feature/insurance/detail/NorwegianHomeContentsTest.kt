package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoFragment.Companion.displayName
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NorwegianHomeContentsTest : TestCase() {
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
    fun shouldShowCoinsuredForNorwegianHomeContentsContract() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS.contracts[0].id
            )
        )

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.YourInfoTab>(0) {
                    recycler {
                        childAt<ContractDetailScreen.YourInfoTab.Header>(0) {
                            text { hasText(R.string.CONTRACT_DETAIL_HOME_TITLE) }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Row>(1) {
                            label { hasText(R.string.CONTRACT_DETAIL_HOME_ADDRESS) }
                            content { hasText("Testvägen 1") }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Row>(2) {
                            label { hasText(R.string.CONTRACT_DETAIL_HOME_POSTCODE) }
                            content { hasText("123 45") }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Row>(3) {
                            label { hasText(R.string.CONTRACT_DETAIL_HOME_TYPE) }
                            content {
                                hasText(
                                    NorwegianHomeContentLineOfBusiness.RENT.displayName(
                                        context()
                                    )
                                )
                            }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Row>(4) {
                            label { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE) }
                            content { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, 50) }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Header>(5) {
                            text { hasText(R.string.CONTRACT_DETAIL_COINSURED_TITLE) }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.Row>(6) {
                            label { hasText(R.string.CONTRACT_DETAIL_COINSURED_TITLE) }
                            content { hasText(R.string.CONTRACT_DETAIL_COINSURED_NUMBER_INPUT, 2) }
                        }
                    }
                }
            }
        }
    }
}
