package com.hedvig.onboarding.messages

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_OR_EXPRESSION
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class OrExpressionTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_OR_EXPRESSION) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowMessageForWhenWithOrExpression() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            messages {
                hasSize(1)
                firstChild<EmbarkScreen.MessageRow> {
                    text { hasText("Or test message that evaluates to true") }
                }
            }
        }
    }
}
