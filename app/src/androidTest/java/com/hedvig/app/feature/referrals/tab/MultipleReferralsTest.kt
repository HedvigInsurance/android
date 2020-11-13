package com.hedvig.app.feature.referrals.tab

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MultipleReferralsTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowActiveStateWhenUserHasMultipleReferrals() {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launchActivity(intent)

        Screen.onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(7)
                childAt<ReferralTabScreen.HeaderItem>(1) {
                    grossPrice {
                        isVisible()
                        hasText(
                            Money.of(349, "SEK")
                                .format(context())
                        )
                    }
                    discountPerMonthPlaceholder { isGone() }
                    newPricePlaceholder { isGone() }
                    discountPerMonth {
                        isVisible()
                        hasText(
                            Money.of(-10, "SEK")
                                .format(context())
                        )
                    }
                    newPrice {
                        isVisible()
                        hasText(
                            Money.of(339, "SEK")
                                .format(context())
                        )
                    }
                    discountPerMonthLabel { isVisible() }
                    newPriceLabel { isVisible() }
                    emptyHeadline { isGone() }
                    emptyBody { isGone() }
                    otherDiscountBox { isGone() }
                }
                childAt<ReferralTabScreen.CodeItem>(2) {
                    placeholder { isGone() }
                    code {
                        isVisible()
                        hasText("TEST123")
                    }
                }
                childAt<ReferralTabScreen.InvitesHeaderItem>(3) {
                    isVisible()
                }
                childAt<ReferralTabScreen.ReferralItem>(4) {
                    iconPlaceholder { isGone() }
                    textPlaceholder { isGone() }
                    name { hasText("Example") }
                    referee { isGone() }
                    icon { hasDrawable(R.drawable.ic_basketball) }
                    status {
                        hasText(
                            Money.of(-10, "SEK")
                                .format(context())
                        )
                    }
                }
                childAt<ReferralTabScreen.ReferralItem>(5) {
                    iconPlaceholder { isGone() }
                    textPlaceholder { isGone() }
                    name { hasText("Example 2") }
                    referee { isGone() }
                    icon { hasDrawable(R.drawable.ic_clock_colorless) }
                }
                childAt<ReferralTabScreen.ReferralItem>(6) {
                    iconPlaceholder { isGone() }
                    textPlaceholder { isGone() }
                    name { hasText("Example 3") }
                    referee { isGone() }
                    icon { hasDrawable(R.drawable.ic_x_in_circle) }
                }
            }
        }
    }
}
