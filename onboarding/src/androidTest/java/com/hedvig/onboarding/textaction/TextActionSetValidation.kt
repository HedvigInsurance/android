package com.hedvig.onboarding.textaction

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.TextActionSetScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionSetValidation : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun textActionSetTest() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        TextActionSetScreen {
            submit { isDisabled() }
            inputs {
                childAt<TextActionSetScreen.Input>(0) {
                    input {
                        edit {
                            hasHint("901124-1234")
                            typeText("9704071234")
                        }
                    }
                }
            }
            submit { isDisabled() }
            inputs {
                childAt<TextActionSetScreen.Input>(1) {
                    input {
                        edit {
                            hasHint("Email")
                            typeText("email@hedvig.com")
                        }
                    }
                }
            }
            submit { isEnabled() }
        }
    }
}
