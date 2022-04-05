package com.hedvig.app.feature.embark.redirect

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_OFFER_REDIRECT
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.LazyIntentsActivityScenarioRule
import com.hedvig.app.util.ValueStoreRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.stub
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import io.github.kakaocup.kakao.screen.Screen.Companion.onScreen
import org.junit.Rule
import org.junit.Test

class OfferRedirectTest : TestCase() {
    @get:Rule
    val activityRule = LazyIntentsActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val compose = createComposeRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_OFFER_REDIRECT) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @get:Rule
    val valueStoreRule = ValueStoreRule("quoteId", "testId")

    @Test
    fun shouldOpenOfferWhenEncounteringRedirectKeys() = run {
        activityRule.launch(EmbarkActivity.newInstance(context(), this::class.java.name, "storyTitle"))

        onScreen<EmbarkScreen> {
            offerActivityIntent { stub() }
            compose
                .onNodeWithTag("SelectActionGrid")
                .onChildren()
                .onFirst()
                .performClick()
            flakySafely { offerActivityIntent { intended() } }
        }
    }
}
