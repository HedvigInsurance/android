package com.hedvig.app.feature.insurance.detail

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.perils.PerilRecyclerItem
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class PerilBottomSheetTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse { success(INSURANCE_DATA_SWEDISH_HOUSE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldExpandBottomSheet() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_SWEDISH_HOUSE.contracts[0].id
            )
        )

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.CoverageTab>(1) {
                    recycler {
                        hasSize(
                            2 +
                                INSURANCE_DATA_SWEDISH_HOUSE
                                    .contracts[0]
                                    .let { it.perils.size + it.insurableLimits.size }
                        )
                        childAt<PerilRecyclerItem>(3) {
                            click()
                        }
                    }
                    onScreen<ContractDetailScreen.CoverageTab.PerilBottomSheetScreen> {
                        chevron.click()
                        sheetRecycler {
                            childAt<ContractDetailScreen.CoverageTab.PerilBottomSheetScreen.Title>(1) {
                                title.hasAnyText()
                            }
                        }
                    }
                }
            }
        }
    }
}
