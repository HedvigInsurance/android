package com.hedvig.app.feature.embark.computedvalues

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.screens.NumberActionScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_COMPUTED_VALUE
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ComputedValuesTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_COMPUTED_VALUE) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldCorrectlyDisplayComputedValues() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name, ""))

        step("Enter value in first passage and submit") {
            NumberActionScreen {
                input {
                    edit {
                        typeText("1334")
                    }
                }
                submit { click() }
            }
        }

        step("Verify that computed value has been correctly parsed") {
            Screen.onScreen<EmbarkScreen> {
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("Computed value is previous input + 3 = 1337") }
                    }
                }
            }
        }
    }
}
