package com.hedvig.app.feature.loggedin


import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.BoundedMatcher
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.util.ApolloCacheClearRule
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class NavBarTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

//    @get:Rule
//    val mockServerRule = ApolloMockServerRule(
//        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
//            success(
//                LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
//            )
//        }
//    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowCorrectIconOnNavBar() {
        activityRule.launchActivity(LoggedInActivity.newInstance(ApplicationProvider.getApplicationContext()))

        onScreen<LoggedInScreen> {
            onView(withId(R.id.bottomNavigation))
                .check(matches(bottomNavNumberOfItems(5)))
        }
    }

    private fun bottomNavNumberOfItems(matcherNumber: Int): Matcher<View?>? {
        return object : BoundedMatcher<View?, BottomNavigationView>(BottomNavigationView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("with $matcherNumber number of items")
            }

            override fun matchesSafely(bottomNavigationView: BottomNavigationView): Boolean {
                return matcherNumber == bottomNavigationView.menu.size()
            }
        }
    }
}

