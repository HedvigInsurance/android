package com.hedvig.onboarding.computedvalues

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_COMPUTED_VALUE
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.screens.NumberActionScreen
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
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldCorrectlyDisplayComputedValues() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this.javaClass.name))

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
