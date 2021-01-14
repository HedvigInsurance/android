package com.hedvig.app.feature.embark.api.graphqlmutation

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.VARIABLE_MUTATION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class SingleVariableTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE) },
        VARIABLE_MUTATION to apolloResponse {
            success(jsonObjectOf("hello" to variables.getString("variable")))
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldRedirectWhenLoadingPassageWithGraphQLMutationApiThatIsError() = run {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        Screen.onScreen<EmbarkScreen> {
            textActionSingleInput { typeText("world") }
            textActionSubmit { click() }
            messages {
                hasSize(1)
                firstChild<EmbarkScreen.MessageRow> {
                    text { hasText("api result: world") }
                }
            }
        }
    }
}
