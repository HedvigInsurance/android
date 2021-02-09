package com.hedvig.app.feature.embark.track

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.embarkTrackerModule
import com.hedvig.app.feature.embark.EmbarkTracker
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TRACK
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.KoinMockModuleRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.json.jsonEq
import com.hedvig.app.util.jsonObjectOf
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
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TRACK) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    private val tracker = mockk<EmbarkTracker>(relaxed = true)

    @get:Rule
    val mockModuleRule = KoinMockModuleRule(
        listOf(embarkTrackerModule),
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
            step("Check that track is called with specific keys") {
                verify(exactly = 1) {
                    tracker.track("Enter third passage",
                        jsonEq(jsonObjectOf("FOO" to "BAR")))
                }
            }
        }
    }
}
