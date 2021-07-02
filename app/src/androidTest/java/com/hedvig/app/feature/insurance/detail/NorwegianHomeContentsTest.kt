package com.hedvig.app.feature.insurance.detail

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.android.owldroid.type.NorwegianHomeContentLineOfBusiness
import com.hedvig.app.R
import com.hedvig.app.feature.insurance.ui.detail.ContractDetailActivity
import com.hedvig.app.testdata.feature.insurance.INSURANCE_DATA_NORWEGIAN_HOME_CONTENTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.stringRes
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NorwegianHomeContentsTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(ContractDetailActivity::class.java)

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
                        childAt<ContractDetailScreen.YourInfoTab.Home>(0) {
                            title { hasText(R.string.CONTRACT_DETAIL_HOME_TITLE) }
                            addressLabel { hasText(R.string.CONTRACT_DETAIL_HOME_ADDRESS) }
                            address { hasText("Testvägen 1") }
                            postCodeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_POSTCODE) }
                            postCode { hasText("123 45") }
                            typeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_TYPE) }
                            type {
                                hasText(
                                    NorwegianHomeContentLineOfBusiness.RENT.stringRes()?.let(context()::getString) ?: ""
                                )
                            }
                            sizeLabel { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE) }
                            size { hasText(R.string.CONTRACT_DETAIL_HOME_SIZE_INPUT, 50) }
                        }

                        childAt<ContractDetailScreen.YourInfoTab.CoInsured>(1) {
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
