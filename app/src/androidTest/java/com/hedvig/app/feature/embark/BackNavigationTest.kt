package com.hedvig.app.feature.embark

import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STANDARD_STORY
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.seconds
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test

class BackNavigationTest : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STANDARD_STORY) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldNavigateBackwardsWhenPressingBackButton() {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )

        onScreen<EmbarkScreen> {
            selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
            await atMost 2.seconds untilAsserted {
                messages {
                    firstChild<EmbarkScreen.MessageRow> { text { hasText("another test message") } }
                }
            }
            pressBack()
            await atMost 2.seconds untilAsserted {
                messages {
                    firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } }
                }
            }
        }
    }
}
