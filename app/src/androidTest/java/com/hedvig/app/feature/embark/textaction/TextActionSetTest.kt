package com.hedvig.app.feature.embark.textaction

import androidx.test.espresso.matcher.ViewMatchers
import com.agoda.kakao.edit.KEditText
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionSetScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasPlaceholderText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionSetTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION_SET) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    val editText1 = KEditText { withMatcher(ViewMatchers.withHint("First Placeholder"))}
    val editText2 = KEditText { withMatcher(ViewMatchers.withHint("Second Placeholder"))}

    @Test
    fun textActionSetTest() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
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
            editText1 {
                typeText("First Text")
                hasHint("First Placeholder")
            }
            submit { isDisabled() }
            editText2 {
                hasHint("Second Placeholder")
                typeText("Second Text")
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
                this.javaClass.name,
                "",
            )
        )

        TextActionSetScreen {
            step("Fill in data and submit") {
                editText1 { replaceText("Test") }
                editText2 { replaceText("Testerson") }
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
                editText1 { replaceText("Test") }
                editText2 { replaceText("Testerson") }
            }
            step("Check that validation passes on prefilled input") {
                submit { isEnabled() }
            }
        }
    }
}
