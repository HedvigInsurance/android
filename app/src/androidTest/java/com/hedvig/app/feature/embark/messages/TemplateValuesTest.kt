package com.hedvig.app.feature.embark.messages

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.hedvig.app.feature.embark.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TemplateValuesTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun shouldResolveTemplateValuesProvidedBySelectAction() {
        MockWebServer().use { webServer ->
            webServer.start(8080)

            webServer.enqueue(
                MockResponse().setBody(
                    """{"data":{"embarkStory":{"__typename":"EmbarkStory","startPassage":"1","passages":[{"__typename":"EmbarkPassage","name":"TestPassage","id":"1","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"123"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"NEVER","text":"Unary false test"}],"text":"Unary false test"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"ALWAYS","text":"Unary true test"}],"text":"Unary true test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage2","label":"Test select action"},"keys":["FOO"],"values":["BAR"]}]}}},{"__typename":"EmbarkPassage","name":"TestPassage2","id":"2","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"another test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"456"},{"__typename":"EmbarkMessage","expressions":[],"text":"{FOO} test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage","label":"Another test select action"},"keys":[],"values":[]}]}}}]}}}"""
                )
            )

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            Screen.onScreen<EmbarkScreen> {
                selectActions {
                    firstChild<EmbarkScreen.SelectAction> {
                        click()
                    }
                }
                messages {
                    childAt<EmbarkScreen.MessageRow>(2) {
                        text {
                            hasText("BAR test")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val INTENT_WITH_STORY_NAME = Intent().apply {
            putExtra(EmbarkActivity.STORY_NAME, "test")
        }
    }
}
