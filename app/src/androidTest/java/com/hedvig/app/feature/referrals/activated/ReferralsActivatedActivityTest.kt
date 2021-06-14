package com.hedvig.app.feature.referrals.activated

import androidx.test.rule.ActivityTestRule
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.app.R
import com.hedvig.app.feature.referrals.ui.activated.ReferralsActivatedActivity
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import io.github.kakaocup.kakao.text.KButton
import io.github.kakaocup.kakao.text.KTextView
import org.javamoney.moneta.Money
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/*
* This test cannot be practically ported to `ActivityScenario` as it causes the test to take 50 seconds to finish.
* Related issue: https://github.com/android/android-test/issues/676
* */
class ReferralsActivatedActivityTest : TestCase() {

    @get:Rule
    val activityRule = ActivityTestRule(ReferralsActivatedActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldCloseWhenPressingButton() = run {
        activityRule.launchActivity(null)

        onScreen<ReferralsActivatedScreen> {
            close { click() }
        }

        assertTrue(activityRule.activity.isFinishing)
    }

    @Test
    fun shouldShowCorrectDiscountAmount() = run {
        activityRule.launchActivity(null)

        onScreen<ReferralsActivatedScreen> {
            body {
                isVisible()
                containsText(
                    Money.of(10, "SEK").format(context(), market())
                )
            }
        }
    }

    class ReferralsActivatedScreen : Screen<ReferralsActivatedScreen>() {
        val close = KButton { withId(R.id.close) }
        val body = KTextView { withId(R.id.body) }
    }
}
