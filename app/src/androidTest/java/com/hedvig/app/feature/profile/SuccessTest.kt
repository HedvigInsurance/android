package com.hedvig.app.feature.profile

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.recycler.KRecyclerItem
import com.agoda.kakao.recycler.KRecyclerView
import com.agoda.kakao.screen.Screen
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.agoda.kakao.text.KTextView
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ProfileQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.profile.PROFILE_DATA
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasText
import org.hamcrest.Matcher
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SuccessTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_REFERRALS_ENABLED
            )
        },
        ProfileQuery.QUERY_DOCUMENT to apolloResponse { success(PROFILE_DATA) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldSuccessfullyLoadProfileTab() {
        activityRule.launchActivity(
            LoggedInActivity.newInstance(
                context(),
                initialTab = LoggedInTabs.PROFILE
            )
        )

        onScreen<ProfileTabScreen> {
            recycler {
                childAt<ProfileTabScreen.Title>(0) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Row>(1) {
                    caption { hasText("Test Testerson") }
                }
                childAt<ProfileTabScreen.Row>(2) {
                    caption { hasText("Example Charity") }
                }
                childAt<ProfileTabScreen.Row>(3) {
                    caption {
                        hasText(
                            R.string.PROFILE_ROW_PAYMENT_DESCRIPTION,
                            Money.of(349, "SEK").format(context())
                        )
                    }
                }
                childAt<ProfileTabScreen.Subtitle>(4) {
                    isVisible()
                }
                childAt<ProfileTabScreen.Row>(5) {
                    isVisible()
                }
            }
        }
    }
}

class ProfileTabScreen : Screen<ProfileTabScreen>() {
    val recycler = KRecyclerView({ withId(R.id.recycler) }, {
        itemType(::Title)
        itemType(::Row)
        itemType(::Subtitle)
    })

    class Title(parent: Matcher<View>) : KRecyclerItem<Title>(parent) {
        val text = KTextView(parent) { withMatcher(parent) }
    }

    class Row(parent: Matcher<View>) : KRecyclerItem<Row>(parent) {
        val caption = KTextView(parent) { withId(R.id.caption) }
    }

    class Subtitle(parent: Matcher<View>) : KRecyclerItem<Subtitle>(parent) {
        val text = KTextView(parent) { withMatcher(parent) }
    }
}
