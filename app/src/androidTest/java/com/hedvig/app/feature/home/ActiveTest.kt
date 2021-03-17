package com.hedvig.app.feature.home

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.home.screens.CommonClaimScreen
import com.hedvig.app.feature.home.screens.EmergencyScreen
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.home.screens.HonestyPledgeSheetScreen
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.hasText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ActiveTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_ACTIVE) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowTitleClaimButtonAndCommonClaimsWhenUserHasOneActiveContract() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.BigTextItem>(0) {
                    text { hasText(R.string.home_tab_welcome_title, "Test") }
                }
                childAt<HomeTabScreen.CommonClaimTitleItem>(3) {
                    isVisible()
                }
                childAt<HomeTabScreen.CommonClaimItem>(4) {
                    text { hasText("Det är kris!") }
                    click()
                }
            }
        }

        onScreen<EmergencyScreen> {
            title { hasTitle("Det är kris!") }
            pressBack()
        }

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.CommonClaimItem>(5) {
                    text { hasText("Trasig telefon") }
                }
                childAt<HomeTabScreen.CommonClaimItem>(6) {
                    text { hasText("Försenat bagage") }
                    click()
                }
            }
        }

        onScreen<CommonClaimScreen> {
            firstMessage { hasText("Försenat bagage") }
            pressBack()
        }

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.StartClaimItem>(1) {
                    button { click() }
                }
            }
        }

        onScreen<HonestyPledgeSheetScreen> {
            claim { isDisplayed() }
        }
    }
}
