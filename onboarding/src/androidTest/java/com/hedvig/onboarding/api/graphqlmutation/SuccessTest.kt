package com.hedvig.onboarding.api.graphqlmutation

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.HELLO_MUTATION
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SuccessTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_GRAPHQL_MUTATION) },
        HELLO_MUTATION to apolloResponse {
            success(jsonObjectOf("hello" to "world"))
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldRedirectAndSaveResultsWhenLoadingPassageWithGraphQLMutationApiThatIsSuccessful() = run {
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
                    text { hasText("api result: world") }
                }
            }
        }
    }
}
