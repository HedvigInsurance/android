package com.hedvig.app.feature.offer

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.ChooseStartDateMutation
import com.hedvig.android.owldroid.graphql.OfferQuery
import com.hedvig.app.feature.offer.ui.OfferActivity
import com.hedvig.app.testdata.feature.offer.OFFER_DATA_SWEDISH_APARTMENT
import com.hedvig.app.testdata.feature.offer.builders.ChooseStartDateBuilder
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.setDate
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.time.LocalDate
import org.junit.Rule
import org.junit.Test

class ChooseStartDateTest : TestCase() {

    private val tomorrow = LocalDate.now().plusDays(1)

    @get:Rule
    val activityRule = LazyActivityScenarioRule(OfferActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        OfferQuery.QUERY_DOCUMENT to apolloResponse { success(OFFER_DATA_SWEDISH_APARTMENT) },
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
    fun shouldShowDatePickerDialogWhenPressingDate() = run {
        activityRule.launch()
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
            materialDatePicker {
                isVisible()
                negativeButton { click() }
            }
        }
    }
}
