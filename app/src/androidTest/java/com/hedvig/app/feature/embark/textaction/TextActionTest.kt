package com.hedvig.app.feature.embark.textaction

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
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
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } } }
            textActionSingleInput {
                isVisible()
                hasHint("Test hint")
            }
            textActionSubmit { isDisabled() }
            textActionSingleInput { typeText("Test entry") }
            textActionSubmit {
                hasText("Another test passage")
                click()
            }
            messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("Test entry was entered") } } }
        }
    }

    @Test
    fun shouldPrefillTextActionWhenUserReturnsToPassage() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            step("Fill out passage and submit") {
                textActionSingleInput { typeText("Foo") }
                textActionSubmit { click() }
            }
            step("Verify that the previous passage no longer is shown") {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("Foo was entered") } } }
            }
            step("Go back and verify that previous answer is prefilled") {
                pressBack()
                textActionSingleInput { hasText("Foo") }
            }
            step("Check that validation passes on prefilled input") {
                textActionSubmit { isEnabled() }
            }
        }
    }
}
