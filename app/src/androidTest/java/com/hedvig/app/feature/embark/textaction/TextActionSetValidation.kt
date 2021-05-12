package com.hedvig.app.feature.embark.textaction

import com.agoda.kakao.edit.KTextInputLayout
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.TextActionSetScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyActivityScenarioRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.withPlaceholder
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionSetValidation : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun textActionSetTest() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name,
                "",
            )
        )

        TextActionSetScreen {
            submit { isDisabled() }
            val editTextPersonalNumber = KTextInputLayout {
                withPlaceholder("901124-1234")
            }
            editTextPersonalNumber {
                edit { typeText("9704071234") }
            }

            submit { isDisabled() }
            val editTextEmail = KTextInputLayout {
                withPlaceholder("example@email.com")
            }
            editTextEmail {
                edit { typeText("email@hedvig.com") }
            }
            submit { isEnabled() }
        }
    }
}
