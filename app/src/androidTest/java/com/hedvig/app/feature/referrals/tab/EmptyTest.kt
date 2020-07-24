package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_NO_DISCOUNTS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apollo.format
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmptyTest {

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.OPERATION_NAME to { LOGGED_IN_DATA_WITH_REFERRALS_FEATURE_ENABLED },
        ReferralsQuery.OPERATION_NAME to { REFERRALS_DATA_WITH_NO_DISCOUNTS }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowEmptyStateWhenLoadedWithNoItems() {
        val intent = LoggedInActivity.newInstance(
            ApplicationProvider.getApplicationContext(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launchActivity(intent)

        Screen.onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(3)
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
                    discountPerMonth { isGone() }
                    newPrice { isGone() }
                    discountPerMonthLabel { isGone() }
                    newPriceLabel { isGone() }
                    emptyHeadline { isVisible() }
                    emptyBody { isVisible() }
                    otherDiscountBox { isGone() }
                }
                childAt<ReferralTabScreen.CodeItem>(2) {
                    placeholder { isGone() }
                    code {
                        isVisible()
                        hasText("TEST123")
                    }
                }
            }
        }
    }
}
