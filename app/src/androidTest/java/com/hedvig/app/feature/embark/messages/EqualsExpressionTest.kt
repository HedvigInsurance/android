package com.hedvig.app.feature.embark.messages

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.apolloResponse
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_EQUALS_EXPRESSION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Rule
import org.junit.Test

class EqualsExpressionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_EQUALS_EXPRESSION) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowMessageForWhenWithEqualsExpression() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
            )
        )

        Screen.onScreen<EmbarkScreen> {
            singleSelectAction { click() }
            messages {
                hasSize(1)
                firstChild<EmbarkScreen.MessageRow> {
                    text {
                        hasText("Binary equals test message that evaluates to true")
                    }
                }
            }
        }
    }
}
