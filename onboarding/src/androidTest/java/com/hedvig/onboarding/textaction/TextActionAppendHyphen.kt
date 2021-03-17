package com.hedvig.onboarding.textaction

import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class TextActionAppendHyphen : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

     @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val mockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldAddHyphenToInput() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), "Story Name"))

        Screen.onScreen<EmbarkScreen> {
            textActionSubmit { isDisabled() }
            textActionSingleInput { typeText("9704071234") }
            textActionSingleInput { hasText("970407-1234") }
            textActionSubmit { isEnabled() }
        }
    }
}

