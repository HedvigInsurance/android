package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.R
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import com.hedvig.app.feature.referrals.builders.LoggedInDataBuilder
import com.hedvig.app.util.apollo.format
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.javamoney.moneta.Money
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class ReferralTabMultipleReferralsTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldShowActiveStateWhenUserHasMultipleReferrals() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(LoggedInDataBuilder().build().toJson())
                    }

                    if (body.contains(ReferralsQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(REFERRALS_DATA.toJson())
                    }

                    return MockResponse()
                }
            }

            webServer.start(8080)

            val intent = LoggedInActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                initialTab = LoggedInTabs.REFERRALS
            )

            activityRule.launchActivity(intent)

            Screen.onScreen<ReferralScreen> {
                share { isVisible() }
                recycler {
                    hasSize(7)
                    childAt<ReferralScreen.HeaderItem>(1) {
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
                        otherDiscountBox { isGone() }
                    }
                    childAt<ReferralScreen.CodeItem>(2) {
                        placeholder { isGone() }
                        code {
                            isVisible()
                            hasText("TEST123")
                        }
                    }
                    childAt<ReferralScreen.InvitesHeaderItem>(3) {
                        isVisible()
                    }
                    childAt<ReferralScreen.ReferralItem>(4) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_basketball) }
                        status {
                            hasText(
                                Money.of(-10, "SEK")
                                    .format(ApplicationProvider.getApplicationContext())
                            )
                        }
                    }
                    childAt<ReferralScreen.ReferralItem>(5) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example 2") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_clock_colorless) }
                    }
                    childAt<ReferralScreen.ReferralItem>(6) {
                        iconPlaceholder { isGone() }
                        textPlaceholder { isGone() }
                        name { hasText("Example 3") }
                        referee { isGone() }
                        icon { hasDrawable(R.drawable.ic_terminated_colorless) }
                    }
                }
            }
        }
    }

    companion object {
        private val REFERRALS_DATA = ReferralsQuery.Data(
            insuranceCost = ReferralsQuery.InsuranceCost(
                fragments = ReferralsQuery.InsuranceCost.Fragments(
                    CostFragment(
                        monthlyDiscount = CostFragment.MonthlyDiscount(
                            fragments = CostFragment.MonthlyDiscount.Fragments(
                                MonetaryAmountFragment(
                                    amount = "10.00",
                                    currency = "SEK"
                                )
                            )
                        ),
                        monthlyNet = CostFragment.MonthlyNet(
                            fragments = CostFragment.MonthlyNet.Fragments(
                                MonetaryAmountFragment(
                                    amount = "339.00",
                                    currency = "SEK"
                                )
                            )
                        ),
                        monthlyGross = CostFragment.MonthlyGross(
                            fragments = CostFragment.MonthlyGross.Fragments(
                                MonetaryAmountFragment(
                                    amount = "349.00",
                                    currency = "SEK"
                                )
                            )
                        )
                    )
                )
            ),
            referralInformation = ReferralsQuery.ReferralInformation(
                campaign = ReferralsQuery.Campaign(
                    code = "TEST123",
                    incentive = ReferralsQuery.Incentive(
                        asMonthlyCostDeduction = ReferralsQuery.AsMonthlyCostDeduction(
                            amount = ReferralsQuery.Amount(
                                fragments = ReferralsQuery.Amount.Fragments(
                                    MonetaryAmountFragment(
                                        amount = "10.00",
                                        currency = "SEK"
                                    )
                                )
                            )
                        )
                    )
                ),
                costReducedIndefiniteDiscount = ReferralsQuery.CostReducedIndefiniteDiscount(
                    fragments = ReferralsQuery.CostReducedIndefiniteDiscount.Fragments(
                        CostFragment(
                            monthlyDiscount = CostFragment.MonthlyDiscount(
                                fragments = CostFragment.MonthlyDiscount.Fragments(
                                    MonetaryAmountFragment(
                                        amount = "10.00",
                                        currency = "SEK"
                                    )
                                )
                            ),
                            monthlyNet = CostFragment.MonthlyNet(
                                fragments = CostFragment.MonthlyNet.Fragments(
                                    MonetaryAmountFragment(
                                        amount = "339.00",
                                        currency = "SEK"
                                    )
                                )
                            ),
                            monthlyGross = CostFragment.MonthlyGross(
                                fragments = CostFragment.MonthlyGross.Fragments(
                                    MonetaryAmountFragment(
                                        amount = "349.00",
                                        currency = "SEK"
                                    )
                                )
                            )
                        )
                    )
                ),
                referredBy = null,
                invitations = listOf(
                    ReferralsQuery.Invitation(
                        fragments = ReferralsQuery.Invitation.Fragments(
                            ReferralFragment(
                                asActiveReferral = ReferralFragment.AsActiveReferral(
                                    name = "Example",
                                    discount = ReferralFragment.Discount(
                                        fragments = ReferralFragment.Discount.Fragments(
                                            MonetaryAmountFragment(
                                                amount = "10.00",
                                                currency = "SEK"
                                            )
                                        )
                                    )
                                ),
                                asInProgressReferral = null,
                                asTerminatedReferral = null
                            )
                        )
                    ),
                    ReferralsQuery.Invitation(
                        fragments = ReferralsQuery.Invitation.Fragments(
                            ReferralFragment(
                                asActiveReferral = null,
                                asInProgressReferral = ReferralFragment.AsInProgressReferral(
                                    name = "Example 2"
                                ),
                                asTerminatedReferral = null
                            )
                        )
                    ),
                    ReferralsQuery.Invitation(
                        fragments = ReferralsQuery.Invitation.Fragments(
                            ReferralFragment(
                                asActiveReferral = null,
                                asInProgressReferral = null,
                                asTerminatedReferral = ReferralFragment.AsTerminatedReferral(
                                    name = "Example 3"
                                )
                            )
                        )
                    )
                )
            )
        )
    }
}
