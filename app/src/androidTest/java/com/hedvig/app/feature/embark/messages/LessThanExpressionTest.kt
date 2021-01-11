package com.hedvig.app.feature.embark.messages

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_LESS_THAN_EXPRESSION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.seconds
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LessThanExpressionTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(STORY_WITH_LESS_THAN_EXPRESSION)
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldShowMessageForWhenWithLessThanExpression() {
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
                    hasSize(1)
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("Less than test message that evaluates to true") }
                    }
                }
            }
        }
    }
}
