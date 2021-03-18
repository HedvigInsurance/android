package com.hedvig.onboarding.textaction

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.screens.TextActionSetScreen
import com.hedvig.testutil.ApolloLocalServerRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionSetTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION_SET) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun textActionSetTest() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        TextActionSetScreen {
            onScreen<EmbarkScreen> {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } } }
            }
            submit {
                hasText("Another test passage")
                isDisabled()
            }
            inputs {
                childAt<TextActionSetScreen.Input>(0) {
                    input {
                        edit {

                            typeText("First Text")
                            hasHint("First Placeholder")
                        }
                    }
                }
            }
            submit { isDisabled() }
            inputs {
                childAt<TextActionSetScreen.Input>(1) {
                    input {
                        edit {
                            hasHint("Second Placeholder")
                            typeText("Second Text")
                        }
                    }
                }
            }
            submit { click() }
            onScreen<EmbarkScreen> {
                messages {
                    firstChild<EmbarkScreen.MessageRow> { text { hasText("First Text Second Text was entered") } }
                }
            }
        }
    }

    @Test
    fun shouldPrefillTextActionSetWhenUserReturnsToPassage() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        TextActionSetScreen {
            step("Fill in data and submit") {
                inputs {
                    childAt<TextActionSetScreen.Input>(0) {
                        input { edit { replaceText("Test") } }
                    }
                    childAt<TextActionSetScreen.Input>(1) {
                        input { edit { replaceText("Testerson") } }
                    }
                }
                submit { click() }
            }
            step("Verify that previous passage is no longer shown") {
                onScreen<EmbarkScreen> {
                    messages {
                        firstChild<EmbarkScreen.MessageRow> {
                            text { hasText("Test Testerson was entered") }
                        }
                    }
                }
            }
            step("Go back and verify that the previous answers are prefilled") {
                pressBack()
                inputs {
                    childAt<TextActionSetScreen.Input>(0) {
                        input { edit { hasText("Test") } }
                    }
                    childAt<TextActionSetScreen.Input>(1) {
                        input { edit { hasText("Testerson") } }
                    }
                }
            }
            step("Check that validation passes on prefilled input") {
                submit { isEnabled() }
            }
        }
    }
}
