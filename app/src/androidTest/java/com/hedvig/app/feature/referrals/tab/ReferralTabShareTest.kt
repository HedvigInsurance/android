package com.hedvig.app.feature.referrals.tab

import android.app.Activity
import android.app.Instrumentation
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsTestRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.CostFragment
import com.hedvig.android.owldroid.fragment.MonetaryAmountFragment
import com.hedvig.android.owldroid.graphql.LoggedInQuery
import com.hedvig.android.owldroid.graphql.ReferralsQuery
import com.hedvig.android.owldroid.type.Feature
import com.hedvig.app.ApolloClientWrapper
import com.hedvig.app.feature.loggedin.ui.LoggedInActivity
import com.hedvig.app.feature.loggedin.ui.LoggedInTabs
import com.hedvig.app.feature.referrals.ReferralScreen
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.inject
import org.koin.test.KoinTest

@RunWith(AndroidJUnit4::class)
class ReferralTabShareTest : KoinTest {
    private val apolloClientWrapper: ApolloClientWrapper by inject()

    @get:Rule
    val activityRule = IntentsTestRule(LoggedInActivity::class.java, false, false)

    @Before
    fun setup() {
        apolloClientWrapper
            .apolloClient
            .clearNormalizedCache()
    }

    @Test
    fun shouldOpenShareWhenClickingShare() {
        MockWebServer().use { webServer ->
            webServer.dispatcher = object : Dispatcher() {
                override fun dispatch(request: RecordedRequest): MockResponse {
                    val body = request.body.peek().readUtf8()
                    if (body.contains(LoggedInQuery.OPERATION_NAME.name())) {
                        return MockResponse().setBody(FEATURES_DATA.toJson())
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

            intending(not(isInternal())).respondWith(
                Instrumentation.ActivityResult(
                    Activity.RESULT_OK,
                    null
                )
            )

            Screen.onScreen<ReferralScreen> {
                share {
                    isVisible()
                    click()
                }
                shareIntent { intended() }
            }
        }
    }

    companion object {
        private val FEATURES_DATA = LoggedInQuery.Data(
            member = LoggedInQuery.Member(
                features = listOf(
                    Feature.KEYGEAR
                )
            ),
            referralTerms = LoggedInQuery.ReferralTerms(
                url = "https://www.example.com"
            )
        )

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
