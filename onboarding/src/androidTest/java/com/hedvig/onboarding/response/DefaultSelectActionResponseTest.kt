package com.hedvig.onboarding.response

import androidx.test.core.app.ApplicationProvider
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test

@Ignore("Causes flakiness")
class DefaultSelectActionResponseTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STANDARD_STORY) }
    )

    @get:Rule
    val apolloCacheClearRule = com.hedvig.testutil.ApolloCacheClearRule()

    @Test
    fun shouldShowResponseAfterSubmittingSelectAction() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            response {
                isVisible()
                hasText("Another test passage")
            }
        }
    }
}
