package com.hedvig.app.feature.home

import android.view.InputDevice
import android.view.MotionEvent
import androidx.test.espresso.action.GeneralClickAction
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Tap
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.R
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.home.screens.HomeTabScreen
import com.hedvig.app.feature.home.ui.HomeAdapter
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_ACTIVE
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ChangeAddressButton : TestCase() {

    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse { success(LOGGED_IN_DATA) },
        HomeQuery.QUERY_DOCUMENT to apolloResponse { success(HOME_DATA_ACTIVE) },
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowChangeAddressButton() = run {
        activityRule.launch(LoggedInActivity.newInstance(context()))

        Screen.onScreen<HomeTabScreen> {
            recycler {
                perform {
                    RecyclerViewActions.actionOnItem<HomeAdapter.ViewHolder.ChangeAddress>(
                        hasDescendant(
                            withText(
                                context().getString(R.string.home_tab_editing_section_change_address_label)
                            )
                        ),
                        GeneralClickAction(
                            Tap.SINGLE, GeneralLocation.VISIBLE_CENTER, Press.FINGER,
                            InputDevice.SOURCE_UNKNOWN, MotionEvent.BUTTON_PRIMARY
                        )
                    )
                }
            }
        }
    }
}
