package com.hedvig.app.feature.embark.redirect

import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_EXTERNAL_REDIRECT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.ValueStoreRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test

class ExternalRedirectOfferTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_EXTERNAL_REDIRECT) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val valueStoreRule = ValueStoreRule("quoteId", "testId")

    @Test
    fun shouldOpenWebOfferWhenEncounteringExternalRedirect() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this::class.java.name, ""))

        onScreen<EmbarkScreen> {
            webOfferIntent { stub() }
            selectActions { childAt<EmbarkScreen.SelectAction>(0) { click() } }
            flakySafely { webOfferIntent { intended() } }
        }
    }
}
