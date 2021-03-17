package com.hedvig.onboarding.api.graphqlquery

import androidx.test.core.app.ApplicationProvider
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.VARIABLE_QUERY
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.StringContainsUUIDMatcher.Companion.containsUUID
import com.hedvig.testutil.apolloResponse
import com.hedvig.app.util.jsonObjectOf
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
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
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldCallGraphQLApiWithVariable() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
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
