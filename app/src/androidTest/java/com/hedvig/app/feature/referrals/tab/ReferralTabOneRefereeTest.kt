package com.hedvig.app.feature.referrals.tab

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.ReferralFragment
import com.hedvig.android.owldroid.graphql.FeaturesQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferralTabOneRefereeTest {
    @get:Rule
    val activityRule = ActivityTestRule(LoggedInActivity::class.java, false, false)

    @Test
    fun shouldShowActiveStateWhenUserHasOneReferee() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains("Features")) {
                        return MockResponse().setBody(FEATURES_DATA.toJson())
                    }

                    if (body.contains("Referrals")) {
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
                    hasSize(4)
                    firstChild<ReferralScreen.HeaderItem> {
                        discountPerMonthPlaceholder { isGone() }
                        newPricePlaceholder { isGone() }
                        discountPerMonth { isGone() }
                        newPrice {
                            isVisible()
                            hasText("")
                        }
                        discountPerMonthLabel { isGone() }
                        newPriceLabel { isGone() }
                        emptyHeadline { isVisible() }
                        emptyBody { isVisible() }
                    }
                    childAt<ReferralScreen.CodeItem>(1) {
                        placeholder { isGone() }
                        code {
                            isVisible()
                            hasText("TEST123")
                        }
                    }
                    childAt<ReferralScreen.InvitesHeaderItem>(2) {
                        isVisible()
                    }
                    childAt<ReferralScreen.ReferralItem>(3) {
                        // TODO
                    }
                }
            }
        }
    }

    companion object {
        private val FEATURES_DATA = FeaturesQuery.Data(
            member = FeaturesQuery.Member(
                features = listOf(
                    Feature.KEYGEAR
                )
            )
        )

        private val REFERRALS_DATA = ReferralsQuery.Data(
            insuranceCost = ReferralsQuery.InsuranceCost(
                fragments = ReferralsQuery.InsuranceCost.Fragments(
                    CostFragment(
                        monthlyDiscount = CostFragment.MonthlyDiscount(
                            amount = "0.00"
                        ),
                        monthlyNet = CostFragment.MonthlyNet(
                            amount = "339.00"
                        ),
                        monthlyGross = CostFragment.MonthlyGross(
                            amount = "349.00"
                        )
                    )
                )
            ),
            referralInformation = ReferralsQuery.ReferralInformation(
                campaign = ReferralsQuery.Campaign(
                    code = "TEST123"
                ),
                referredBy = ReferralsQuery.ReferredBy(
                    fragments = ReferralsQuery.ReferredBy.Fragments(
                        ReferralFragment(
                            asActiveReferral = ReferralFragment.AsActiveReferral(
                                name = "Example Exampleson",
                                discount = ReferralFragment.Discount(
                                    amount = "10.00"
                                )
                            ),
                            asAcceptedReferral = null,
                            asInProgressReferral = null,
                            asTerminatedReferral = null
                        )
                    )
                ),
                invitations = emptyList()
            )
        )
    }
}
