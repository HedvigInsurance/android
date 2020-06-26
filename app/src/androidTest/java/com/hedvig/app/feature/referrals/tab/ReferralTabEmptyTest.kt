package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.app.ApolloClientWrapper
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
class ReferralTabEmptyTest : KoinTest {
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
    fun shouldShowEmptyStateWhenLoadedWithNoItems() {
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
                    hasSize(3)
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
                        discountPerMonth { isGone() }
                        newPrice { isGone() }
                        discountPerMonthLabel { isGone() }
                        newPriceLabel { isGone() }
                        emptyHeadline { isVisible() }
                        emptyBody { isVisible() }
                        otherDiscountBox { isGone() }
                    }
                    childAt<ReferralScreen.CodeItem>(2) {
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

    companion object {
        private val REFERRALS_DATA = ReferralsQuery.Data(
            insuranceCost = ReferralsQuery.InsuranceCost(
                fragments = ReferralsQuery.InsuranceCost.Fragments(
                    CostFragment(
                        monthlyDiscount = CostFragment.MonthlyDiscount(
                            fragments = CostFragment.MonthlyDiscount.Fragments(
                                MonetaryAmountFragment(
                                    amount = "0.00",
                                    currency = "SEK"
                                )
                            )
                        ),
                        monthlyNet = CostFragment.MonthlyNet(
                            fragments = CostFragment.MonthlyNet.Fragments(
                                MonetaryAmountFragment(
                                    amount = "349.00",
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
                                        amount = "0.00",
                                        currency = "SEK"
                                    )
                                )
                            ),
                            monthlyNet = CostFragment.MonthlyNet(
                                fragments = CostFragment.MonthlyNet.Fragments(
                                    MonetaryAmountFragment(
                                        amount = "349.00",
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
                invitations = emptyList()
            )
        )
    }
}
