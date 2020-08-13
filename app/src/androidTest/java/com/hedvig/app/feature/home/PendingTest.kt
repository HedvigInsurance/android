package com.hedvig.app.feature.home

import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.testdata.feature.home.HOME_DATA_PENDING
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import org.hamcrest.Matcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
        HomeQuery.OPERATION_NAME to { HOME_DATA_PENDING }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowMessageWhenUserHasAllContractsInPendingState() {
        activityRule.launchActivity(LoggedInActivity.newInstance(ApplicationProvider.getApplicationContext()))

        onScreen<HomeTabScreen> {
            recycler {
                childAt<HomeTabScreen.BigTextItem>(0) {
                    text { hasText("Test TODO") }
                }
                childAt<HomeTabScreen.BodyTextItem>(1) {
                    text { hasText(R.string.home_tab_pending_switchable_body) }
                }
            }
        }
    }
}

class HomeTabScreen : Screen<HomeTabScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::BigTextItem)
        itemType(::BodyTextItem)
    })

    class BigTextItem(parent: Matcher<View>) : KRecyclerItem<BigTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }

    class BodyTextItem(parent: Matcher<View>) : KRecyclerItem<BodyTextItem>(parent) {
        val text = KTextView { withMatcher(parent) }
    }
}
