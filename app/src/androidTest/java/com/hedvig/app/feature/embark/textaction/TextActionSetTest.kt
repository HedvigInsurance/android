package com.hedvig.app.feature.embark.textaction

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionSetScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.hedvig.app.util.withPlaceholder
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.edit.KTextInputLayout
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
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

    val input1 = KTextInputLayout { withPlaceholder("Placeholder") }
    val input2 = KTextInputLayout { withPlaceholder("Second Placeholder") }

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
            input1 {
                edit { typeText("First Text") }
            }
            submit { isDisabled() }
            input2 {
                edit { typeText("Second Text") }
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
                input1 { edit { replaceText("Test") } }
                input2 { edit { replaceText("Testerson") } }
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
                input1 { edit { replaceText("Test") } }
                input2 { edit { replaceText("Testerson") } }
            }
            step("Check that validation passes on prefilled input") {
                submit { isEnabled() }
            }
        }
    }

    @Test
    fun shouldTriggerCorrectActionsForImeButton() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
            )
        )

        TextActionSetScreen {
            step("Focus first field and fill in data") {
                input1 {
                    edit {
                        click()
                        typeText("Test")
                    }
                }
            }
            step("Press IME Button") {
                input1 { edit { pressImeAction() } }
            }
            step("Check that second field has focus") {
                input2 { edit { isFocused() } }
            }
            step("Fill out second field") {
                input2 { edit { typeText("Testerson") } }
            }
            step("Press IME Button") {
                input2 { edit { pressImeAction() } }
            }
            step("Verify that next passage is shown") {
                onScreen<EmbarkScreen> {
                    messages {
                        childAt<EmbarkScreen.MessageRow>(0) {
                            text { hasText("Test Testerson was entered") }
                        }
                    }
                }
            }
        }
    }
}
