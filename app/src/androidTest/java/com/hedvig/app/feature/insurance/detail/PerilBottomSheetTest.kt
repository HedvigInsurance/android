package com.hedvig.app.feature.insurance.detail

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerilBottomSheetTest {
    @get:Rule
    val activityRule = ActivityTestRule(ContractDetailActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse { success(INSURANCE_DATA_SWEDISH_HOUSE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldExpandBottomSheet() {
        activityRule.launchActivity(
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
                            2
                                + INSURANCE_DATA_SWEDISH_HOUSE.contracts[0].let { it.perils.size + it.insurableLimits.size }
                        )
                        childAt<ContractDetailScreen.CoverageTab.Peril>(3) {
                            click()
                        }
                    }
                    onScreen<ContractDetailScreen.CoverageTab.BottomSheetScreen> {
                        chevron.click()
                        sheetRecycler {
                            childAt<ContractDetailScreen.CoverageTab.BottomSheetScreen.Title>(1) {
                                title.hasAnyText()
                            }
                        }
                    }
                }
            }
        }
    }
}
