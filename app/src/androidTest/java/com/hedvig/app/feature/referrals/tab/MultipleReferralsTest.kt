package com.hedvig.app.feature.referrals.tab

import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.campaignModule
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ui.tab.CampaignUseCase
import com.hedvig.app.testdata.feature.referrals.LOGGED_IN_DATA_WITH_KEY_GEAR_FEATURE_ENABLED
import com.hedvig.app.testdata.feature.referrals.REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.market
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import io.mockk.coEvery
import io.mockk.mockk
import io.reactivex.schedulers.Schedulers.single
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class MultipleReferralsTest : TestCase() {

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
                REFERRALS_DATA_WITH_MULTIPLE_REFERRALS_IN_DIFFERENT_STATES
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val mockCampaignUseCase = mockk<CampaignUseCase>()

    @get:Rule
    val mockCampaignRule = KoinMockModuleRule(
        listOf(campaignModule),
        listOf(module { single { mockCampaignUseCase } })
    )

    @Before
    fun setup() {
        coEvery { mockCampaignUseCase.shouldShowCampaign() }.returns(false)
    }

    @Test
    fun shouldShowActiveStateWhenUserHasMultipleReferrals() = run {
        val intent = LoggedInActivity.newInstance(
            context(),
            initialTab = LoggedInTabs.REFERRALS
        )

        activityRule.launch(intent)

        Screen.onScreen<ReferralTabScreen> {
            share { isVisible() }
            recycler {
                hasSize(7)
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
                    // icon { hasDrawable(R.drawable.ic_basketball) } // This assertion fails incorrectly on Kakao 2.4.0
                    status {
                        hasText(
                            Money.of(-10, "SEK")
                                .format(context(), market())
                        )
                    }
                }
                childAt<ReferralTabScreen.ReferralItem>(5) {
                    iconPlaceholder { isGone() }
                    textPlaceholder { isGone() }
                    name { hasText("Example 2") }
                    referee { isGone() }
                    // icon { hasDrawable(R.drawable.ic_clock_colorless) } // This assertion fails incorrectly on Kakao 2.4.0
                }
                childAt<ReferralTabScreen.ReferralItem>(6) {
                    iconPlaceholder { isGone() }
                    textPlaceholder { isGone() }
                    name { hasText("Example 3") }
                    referee { isGone() }
                    // icon { hasDrawable(R.drawable.ic_x_in_circle) } // This assertion fails incorrectly on Kakao 2.4.0
                }
            }
        }
    }
}
