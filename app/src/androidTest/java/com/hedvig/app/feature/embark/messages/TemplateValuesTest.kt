package com.hedvig.app.feature.embark.messages

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEMPLATE_MESSAGE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TemplateValuesTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEMPLATE_MESSAGE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldResolveTemplateValuesProvidedBySelectAction() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
            )
        )

        Screen.onScreen<EmbarkScreen> {
            selectActions {
                firstChild<EmbarkScreen.SelectAction> {
                    click()
                }
            }
            messages {
                firstChild<EmbarkScreen.MessageRow> {
                    text {
                        hasText("BAR test")
                    }
                }
            }
        }
    }
}
