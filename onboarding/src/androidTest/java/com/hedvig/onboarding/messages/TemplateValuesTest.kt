package com.hedvig.onboarding.messages

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEMPLATE_MESSAGE
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
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
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldResolveTemplateValuesProvidedBySelectAction() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
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
