package com.hedvig.app.feature.embark.messages

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_UNARY_EXPRESSIONS
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnaryExpressionTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_UNARY_EXPRESSIONS) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun shouldFilterOutMessagesWithExpressionTypeNever() {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                ApplicationProvider.getApplicationContext(),
                this.javaClass.name
            )
        )

        Screen.onScreen<EmbarkScreen> {
            messages {
                hasSize(1)
                firstChild<EmbarkScreen.MessageRow> {
                    text {
                        hasText("Unary true test")
                    }
                }
            }
        }
    }
}
