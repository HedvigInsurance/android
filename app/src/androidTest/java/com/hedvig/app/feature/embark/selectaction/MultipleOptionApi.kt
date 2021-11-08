package com.hedvig.app.feature.embark.selectaction

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.HELLO_QUERY
import com.hedvig.app.testdata.feature.embark.data.STANDARD_THIRD_MESSAGE
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class MultipleOptionApi : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS) },
        HELLO_QUERY to apolloResponse {
            success(jsonObjectOf("hello" to "world"))
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun whenSubmittingSelectActionWithApiShouldCallApi() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), "", ""))

        onScreen<EmbarkScreen> {
            step("Click select option with API") {
                compose
                    .onNodeWithTag("SelectActionGrid")
                    .onChildren()
                    .get(1)
                    .performClick()
            }
            step("Verify that success-passage from API is redirected to") {
                messages {
                    childAt<EmbarkScreen.MessageRow>(0) { text { hasText(STANDARD_THIRD_MESSAGE.text) } }
                }
            }
        }
    }
}
