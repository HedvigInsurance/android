package com.hedvig.app.feature.insurance.detail

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.SwedishApartmentLineOfBusiness
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.insurance.ui.detail.yourinfo.YourInfoFragment.Companion.displayName
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_APARTMENT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SwedishApartmentTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(ContractDetailActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse { success(INSURANCE_DATA_SWEDISH_APARTMENT) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowCoinsuredForSwedishApartmentContract() = run {
        activityRule.launchActivity(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_SWEDISH_APARTMENT.contracts[0].id
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
                                    SwedishApartmentLineOfBusiness.RENT.displayName(
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
