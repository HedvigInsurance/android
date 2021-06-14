package com.hedvig.app.feature.embark.api.graphqlquery

import androidx.test.core.app.ApplicationProvider
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.VARIABLE_QUERY
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.StringContainsUUIDMatcher.Companion.containsUUID
import com.hedvig.app.apolloResponse
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class GeneratedVariableTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE)
        },
        VARIABLE_QUERY to apolloResponse {
            success(jsonObjectOf("hello" to variables.getString("variable")))
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldCallGraphQLApiWithVariable() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name,
                "",
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            messages {
                hasSize(2)
                childAt<EmbarkScreen.MessageRow>(0) {
                    text { hasText(containsUUID()) }
                }
                childAt<EmbarkScreen.MessageRow>(1) {
                    text { hasText(containsUUID()) }
                }
            }
        }
    }
}
