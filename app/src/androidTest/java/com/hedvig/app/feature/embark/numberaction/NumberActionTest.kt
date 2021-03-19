package com.hedvig.app.feature.embark.numberaction

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.NumberActionScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_NUMBER_ACTION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.hasHelperText
import com.hedvig.app.util.hasPlaceholderText
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class NumberActionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_NUMBER_ACTION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRenderNumberAction() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

        NumberActionScreen {
            step("Check that labels match data") {
                submit {
                    isDisabled()
                    hasText("Another test passage")
                }
                input {
                    hasHint("Co-insured")
                    hasPlaceholderText("1")
                    hasHelperText("other people")
                }
            }
            step("Test that lower bound does not allow submit") {
                input {
                    edit {
                        typeText("0")
                    }
                }
                submit { isDisabled() }
            }
            step("Test that upper bound does not allow submit") {
                input { edit { replaceText("100") } }
                submit { isDisabled() }
            }
            step("Moving from valid to empty does not allow submit") {
                input { edit { replaceText("50") } }
                submit { isEnabled() }
                input { edit { replaceText("") } }
                submit { isDisabled() }
            }
            step("Test that number in range allows submit") {
                input { edit { replaceText("50") } }
                submit {
                    click()
                }
            }
            step("Verify that value has been recorded in store") {
                onScreen<EmbarkScreen> {
                    messages {
                        childAt<EmbarkScreen.MessageRow>(0) { text { hasText("50 was entered") } }
                    }
                }
            }
        }
    }

    @Test
    fun shouldPrefillNumberActionWhenUserReturnsToPassage() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

        NumberActionScreen {
            step("Fill out passage and submit") {
                input { edit { typeText("50") } }
                submit { click() }
            }
            step("Verify that the previous passage is no longer shown") {
                onScreen<EmbarkScreen> {
                    messages {
                        childAt<EmbarkScreen.MessageRow>(0) { text { hasText("50 was entered") } }
                    }
                }
            }
            step("Go back and verify that the previous answer is prefilled") {
                pressBack()
                input { edit { hasText("50") } }
            }
            step("Check that validation passes on prefilled input") {
                submit { isEnabled() }
            }
        }
    }
}
