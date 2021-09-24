package com.hedvig.app.feature.referrals.tab

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.javamoney.moneta.Money
import org.junit.Rule
import org.junit.Test

class OtherDiscountTest : TestCase() {

    @get:Rule
    val activityRule = LazyActivityScenarioRule(LoggedInActivity::class.java)

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        LoggedInQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
            )
        },
        ReferralsQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                REFERRALS_DATA_WITH_ONE_REFEREE_AND_OTHER_DISCOUNT
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowOtherDiscountWhenUserHasNonReferralDiscounts() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launch(intent)

        onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(5)
                childAt<ReferralTabScreen.HeaderItem>(1) {
                    grossPrice {
                        isVisible()
                        hasText(
                            Money.of(349, "SEK")
                                .format(context(), market())
                        )
                    }
                    discountPerMonthPlaceholder { isGone() }
                    newPricePlaceholder { isGone() }
                    discountPerMonth {
                        isVisible()
                        hasText(
                            Money.of(-10, "SEK")
                                .format(context(), market())
                        )
                    }
                    newPrice {
                        isVisible()
                        hasText(
                            Money.of(339, "SEK")
                                .format(context(), market())
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
                        // hasDrawable(R.drawable.ic_basketball) // This assertion fails incorrectly on Kakao 2.4.0
                    }
                    status {
                        hasText(
                            Money.of(-10, "SEK")
                                .format(context(), market())
                        )
                    }
                }
            }
        }
    }
}
