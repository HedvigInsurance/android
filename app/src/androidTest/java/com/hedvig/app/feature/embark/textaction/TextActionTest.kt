package com.hedvig.app.feature.embark.textaction

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.TextActionScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRenderTextAction() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                storyTitle
            )
        )

        TextActionScreen {
            onScreen<EmbarkScreen> {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } } }
            }
            input { hasHint("Test hint") }
            submitButton {
                hasText("Another test passage")
                isDisabled()
            }
            input { edit { typeText("Test entry") } }
            submitButton { click() }
            onScreen<EmbarkScreen> {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("Test entry was entered") } } }
            }
        }
    }

    @Test
    fun shouldPrefillTextActionWhenUserReturnsToPassage() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                storyTitle
            )
        )

        TextActionScreen {
            step("Fill out passage and submit") {
                input { edit { typeText("Foo") } }
                submitButton { click() }
            }
            step("Verify that the previous passage no longer is shown") {
                onScreen<EmbarkScreen> {
                    messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("Foo was entered") } } }
                }
            }
            step("Go back and verify that previous answer is prefilled") {
                pressBack()
                input { edit { hasText("Foo") } }
            }
            step("Check that validation passes on prefilled input") {
                submitButton { isEnabled() }
            }
        }
    }
}
