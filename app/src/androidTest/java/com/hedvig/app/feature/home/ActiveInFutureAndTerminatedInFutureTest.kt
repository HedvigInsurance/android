package com.hedvig.app.feature.home

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ActiveInFutureAndTerminatedInFutureTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                HOME_DATA_ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowMessageWhenUserHasAllContractsInActiveInFutureStateOrActiveInFutureAndTerminatedInFutureState() =
        run {
            activityRule.launch(LoggedInActivity.newInstance(context()))
            val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

            onScreen<HomeTabScreen> {
                recycler {
                    childAt<HomeTabScreen.BigTextItem>(0) {
                        text {
                            hasText(
                                R.string.home_tab_active_in_future_welcome_title,
                                "Test",
                                formatter.format(LocalDate.of(2024, 1, 1))
                            )
                        }
                    }
                    childAt<HomeTabScreen.BodyTextItem>(1) {
                        text { hasText(R.string.home_tab_active_in_future_body) }
                    }
                }
            }
        }
}
