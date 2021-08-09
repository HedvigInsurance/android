package com.hedvig.app.feature.insurance.detail

import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_SWEDISH_HOUSE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.MarketRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class SwedishHouseTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        InsuranceQuery.QUERY_DOCUMENT to apolloResponse { success(INSURANCE_DATA_SWEDISH_HOUSE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val marketRule = MarketRule(Market.SE)

    @Test
    fun shouldShowCoinsuredForSwedishHouseContract() = run {
        activityRule.launch(
            ContractDetailActivity.newInstance(
                context(),
                INSURANCE_DATA_SWEDISH_HOUSE.contracts[0].id
            )
        )

        onScreen<ContractDetailScreen> {
            tabContent {
                childAt<ContractDetailScreen.YourInfoTab>(0) {
                    recycler {
                        childAt<ContractDetailScreen.YourInfoTab.Home>(0) {
                            title { hasText(R.string.CONTRACT_DETAIL_HOME_TITLE) }
                            addressLabel { hasText(R.string.CONTRACT_DETAIL_HOME_ADDRESS) }
                            address { hasText("Testvägen 1") }
                            postCodeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_POSTCODE) }
                            postCode { hasText("123 45") }
                            typeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_TYPE) }
                            type { hasText(R.string.SWEDISH_HOUSE_LOB) }
                            sizeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE) }
                            size { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, 50) }
                        }
                        childAt<ContractDetailScreen.YourInfoTab.CoInsured>(2) {
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
