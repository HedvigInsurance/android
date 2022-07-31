package com.hedvig.app.feature.embark.api.graphqlmutation

import com.hedvig.android.core.common.jsonObjectOf
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE
import com.hedvig.app.testdata.feature.embark.data.VARIABLE_MUTATION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen
import org.junit.Rule
import org.junit.Test

class SingleVariableTest : TestCase() {
  @get:Rule
  val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

  @get:Rule
  val apolloMockServerRule = ApolloMockServerRule(
    EmbarkStoryQuery.OPERATION_DOCUMENT to apolloResponse {
      success(STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE)
    },
    VARIABLE_MUTATION to apolloResponse {
      success(jsonObjectOf("hello" to variables.getString("variable")))
    },
  )

  @get:Rule
  val apolloCacheClearRule = ApolloCacheClearRule()

  @Test
  fun shouldRedirectWhenLoadingPassageWithGraphQLMutationWithSingleVariable() = run {
    activityRule.launch(
      EmbarkActivity.newInstance(
        context(),
        this.javaClass.name,
        "",
      ),
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
