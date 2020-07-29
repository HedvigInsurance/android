package com.hedvig.app.feature.referrals.activated

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KButton
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import org.javamoney.moneta.Money
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralsActivatedActivityTest {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsActivatedActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldCloseWhenPressingButton() {
        activityRule.launchActivity(null)

        onScreen<ReferralsActivatedScreen> {
            close { click() }
        }

        assertTrue(activityRule.activity.isFinishing)
    }

    @Test
    fun shouldShowCorrectDiscountAmount() {
        activityRule.launchActivity(null)

        onScreen<ReferralsActivatedScreen> {
            body {
                isVisible()
                containsText(
                    Money.of(10, "SEK").format(ApplicationProvider.getApplicationContext())
                )
            }
        }
    }

    class ReferralsActivatedScreen : Screen<ReferralsActivatedScreen>() {
        val close = KButton { withId(R.id.close) }
        val body = KTextView { withId(R.id.body) }
    }
}
