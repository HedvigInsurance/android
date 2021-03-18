package com.hedvig.onboarding

import androidx.test.core.app.ApplicationProvider
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.onboarding.createoffer.EmbarkActivity
import com.hedvig.onboarding.screens.EmbarkScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.apolloModule
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.testutil.ApolloMockServerRule
import com.hedvig.testutil.LazyActivityScenarioRule
import com.hedvig.testutil.apolloResponse
import com.hedvig.testutil.ApolloCacheClearRule
import com.hedvig.testutil.ApolloLocalServerRule
import com.hedvig.testutil.apolloTestModule
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

class BackNavigationTest : TestCase() {
    @get:Rule
    val activityRule = LazyActivityScenarioRule(EmbarkActivity::class.java)

    @get:Rule
    val apolloLocalServerRule = ApolloLocalServerRule()

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STANDARD_STORY) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldNavigateBackwardsWhenPressingBackButton() = run {
        activityRule.launch(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            messages {
                firstChild<EmbarkScreen.MessageRow> { text { hasText("another test message") } }
            }
            pressBack()
            messages {
                firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } }
            }
        }
    }
}
