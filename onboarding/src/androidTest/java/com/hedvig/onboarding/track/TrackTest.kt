package com.hedvig.onboarding.track

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TRACK
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.KoinMockModuleRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.app.util.jsonObjectOf
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.createoffer.EmbarkModule
import com.hedvig.onboarding.createoffer.EmbarkTracker
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.jsonEq
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module

class TrackTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TRACK) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    private val tracker = mockk<EmbarkTracker>(relaxed = true)

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(EmbarkModule.trackerModule),
        listOf(module { single { tracker } })
    )

    @Test
    fun shouldTrackWhenEnteringPassage() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

        onScreen<EmbarkScreen> {
            step("Check that initial passage-track is tracked") {
                verify(exactly = 1) { tracker.track("Enter Passage") }
            }
            step("Navigate to next passage, populating store with data") {
                selectActions {
                    childAt<EmbarkScreen.SelectAction>(0) { click() }
                }
            }
            step("Verify that next passage is showing") {
                messages {
                    childAt<EmbarkScreen.MessageRow>(0) {
                        text { hasText("another test message") }
                    }
                }
            }
            step("Check that track is called with all store data") {
                verify(exactly = 1) {
                    tracker.track("Enter second passage",
                        jsonEq(
                            jsonObjectOf(
                                "FOO" to "BAR",
                                "BAZ" to "BAT",
                                "TestPassageResult" to "Another test passage",
                            )
                        )
                    )
                }
            }
            step("Navigate to next passage") {
                selectActions {
                    childAt<EmbarkScreen.SelectAction>(0) { click() }
                }
            }
            step("Verify that next passage is showing") {
                messages {
                    childAt<EmbarkScreen.MessageRow>(0) {
                        text { hasText("a third message") }
                    }
                }
            }
            step("Check that track is called with specific keys, and that customData is merged in") {
                verify(exactly = 1) {
                    tracker.track("Enter third passage",
                        jsonEq(jsonObjectOf("FOO" to "BAR", "CUSTOM" to "DATA")))
                }
            }
            step("Go back") {
                pressBack()
            }
            step("Check that track is called for back navigation") {
                verify(exactly = 1) {
                    tracker.track("Passage Go Back - TestPassage3")
                }
            }
        }
    }
}
