package com.hedvig.app.feature.offer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
import com.hedvig.app.testdata.feature.offer.builders.ChooseStartDateBuilder
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.util.setDate
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class ChooseStartDateExistingSwitchableInsuranceTest : TestCase() {

    private val tomorrow = LocalDate.now().plusDays(1)

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                OFFER_DATA_SWEDISH_APARTMENT_WITH_CURRENT_INSURER_SWITCHABLE
            )
        },
        ChooseStartDateMutation.QUERY_DOCUMENT to apolloResponse {
            success(
                ChooseStartDateBuilder(
                    date = tomorrow
                ).build()
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldUpdateStartDateWhenChoosingStartDate() = run {
        activityRule.launch()

        val tomorrow = LocalDate.now().plusDays(1)

        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate {
                        click()
                    }
                }
            }
        }
        onScreen<ChangeDateSheet> {
            pickDate { click() }
            datePicker {
                datePicker { setDate(tomorrow) }
                okButton { click() }
            }
            submit { click() }
            confirm { positiveButton { click() } }
        }
        onScreen<OfferScreen> {
            scroll {
                childAt<OfferScreen.HeaderItem>(0) {
                    startDate { containsText(tomorrow.toString()) }
                }
            }
        }
    }
}
