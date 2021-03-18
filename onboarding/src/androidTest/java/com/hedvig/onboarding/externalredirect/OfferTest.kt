package com.hedvig.onboarding.externalredirect

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_EXTERNAL_REDIRECT
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyIntentsActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.context
import com.hedvig.testutil.stub
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.testutil.ApolloLocalServerRule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class OfferTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_EXTERNAL_REDIRECT) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldOpenWebOfferWhenEncounteringExternalRedirect() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this::class.java.name))

        onScreen<EmbarkScreen> {
            offer { stub() }
            selectActions { childAt<EmbarkScreen.SelectAction>(0) { click() } }
            flakySafely { offer { intended() } }
        }
    }
}
