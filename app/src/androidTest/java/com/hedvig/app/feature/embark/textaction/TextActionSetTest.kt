package com.hedvig.app.feature.embark.textaction

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.data.STORY_WITH_TEXT_ACTION_SET
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.hedvig.app.util.seconds
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TextActionSetTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse { success(STORY_WITH_TEXT_ACTION_SET) }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun textActionSetTest() {
        activityRule.launchActivity(
            EmbarkActivity.newInstance(
                context(),
                this.javaClass.name
            )
        )

        Screen.onScreen<EmbarkScreen> {
            messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("test message") } } }
            textActionSubmit { isDisabled() }
            textActionSet {
                childAt<EmbarkScreen.TextAction>(0) {
                    input {
                        typeText("First Text")
                        hasHint("First Placeholder")
                    }
                }
            }
            textActionSubmit { isDisabled() }
            textActionSet {
                childAt<EmbarkScreen.TextAction>(1) {
                    input {
                        hasHint("Second Placeholder")
                        typeText("Second Text")
                    }
                }
            }
            textActionSubmit {
                hasText("Another test passage")
                click()
            }
            await atMost 2.seconds untilAsserted {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("First Text Second Text was entered") } } }
            }
        }
    }
}
