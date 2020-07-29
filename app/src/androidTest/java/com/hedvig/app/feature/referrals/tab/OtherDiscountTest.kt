package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OtherDiscountTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED)
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse {
            success(REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowOtherDiscountWhenUserHasNonReferralDiscounts() {
        val intent = LoggedInActivity.newInstance(
            ApplicationProvider.getApplicationContext(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launchActivity(intent)

        onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(5)
                childAt<ReferralTabScreen.HeaderItem>(1) {
                    grossPrice {
                        isVisible()
                        hasText(
                            Money.of(349, "SEK")
                                .format(ApplicationProvider.getApplicationContext())
                        )
                    }
                    discountPerMonthPlaceholder { isGone() }
                    newPricePlaceholder { isGone() }
                    discountPerMonth {
                        isVisible()
                        hasText(
                            Money.of(-10, "SEK")
                                .format(ApplicationProvider.getApplicationContext())
                        )
                    }
                    newPrice {
                        isVisible()
                        hasText(
                            Money.of(339, "SEK")
                                .format(ApplicationProvider.getApplicationContext())
                        )
                    }
                    discountPerMonthLabel { isVisible() }
                    newPriceLabel { isVisible() }
                    emptyHeadline { isGone() }
                    emptyBody { isGone() }
                    otherDiscountBox { isVisible() }
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
                    name {
                        isVisible()
                        hasText("Example")
                    }
                    referee { isVisible() }
                    icon {
                        isVisible()
                        hasDrawable(R.drawable.ic_basketball)
                    }
                    status {
                        hasText(
                            Money.of(-10, "SEK")
                                .format(ApplicationProvider.getApplicationContext())
                        )
                    }
                }
            }
        }
    }
}
