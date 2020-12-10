package com.hedvig.app.feature.embark.textaction

import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import com.hedvig.app.feature.embark.ui.EmbarkActivity
import com.hedvig.app.testdata.feature.embark.STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
import com.hedvig.app.util.ApolloCacheClearRule
import com.hedvig.app.util.ApolloMockServerRule
import com.hedvig.app.util.apolloResponse
import com.hedvig.app.util.context
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import org.awaitility.Durations
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test

class TextActionSetValidation : TestCase() {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @get:Rule
    val apolloMockServerRule = ApolloMockServerRule(
        EmbarkStoryQuery.QUERY_DOCUMENT to apolloResponse {
            success(
                STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION
            )
        }
    )

    @get:Rule
    val apolloCacheClearRule = ApolloCacheClearRule()

    @Test
    fun textActionSetTest() = run {
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
                        typeText("9704071234")
                        hasHint("901124-1234")
                    }
                }
            }
            textActionSubmit { isDisabled() }
            textActionSet {
                childAt<EmbarkScreen.TextAction>(1) {
                    input {
                        hasHint("Email")
                        typeText("email@hedvig.com")
                    }
                }
            }
            textActionSubmit {
                hasText("Another test passage")
                click()
            }
            await atMost Durations.TWO_SECONDS untilAsserted {
                messages { firstChild<EmbarkScreen.MessageRow> { text { hasText("970407-1234 email@hedvig.com was entered") } } }
            }
        }
    }
}
