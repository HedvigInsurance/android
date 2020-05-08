package com.hedvig.app.feature.embark

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EmbarkActivityTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun showsSpinnerWhileLoading() {
        activityRule.launchActivity(INTENT_WITH_STORY_NAME)
        onScreen<EmbarkScreen> {
            spinner {
                isVisible()
            }
        }
    }

    @Test
    fun endsActivityIfNoStoryNameIsProvided() {
        activityRule.launchActivity(Intent())
        assertTrue(activityRule.activity.isFinishing)
        assertTrue(activityRule.activityResult.resultCode == Activity.RESULT_CANCELED)
    }

    @Test
    fun showsFirstPassageWhenNetworkHasLoaded() {
        MockWebServer().use { webServer ->
            webServer.start(8080)

            webServer.enqueue(
                MockResponse().setBody(
                    """{"data":{"embarkStory":{"__typename":"EmbarkStory","startPassage":"1","passages":[{"__typename":"EmbarkPassage","name":"TestPassage","id":"1","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"123"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"NEVER","text":"Unary false test"}],"text":"Unary false test"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"ALWAYS","text":"Unary true test"}],"text":"Unary true test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage2","label":"Test select action"},"keys":["FOO"],"values":["BAR"]}]}}},{"__typename":"EmbarkPassage","name":"TestPassage2","id":"2","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"another test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"456"},{"__typename":"EmbarkMessage","expressions":[],"text":"{FOO} test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage","label":"Another test select action"},"keys":[],"values":[]}]}}}]}}}"""
                )
            )

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            onScreen<EmbarkScreen> {
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text {
                            hasText("test message")
                        }
                    }
                    childAt<EmbarkScreen.MessageRow>(1) {
                        text {
                            hasText("123")
                        }
                    }
                }
                selectActions {
                    firstChild<EmbarkScreen.SelectAction> {
                        button {
                            hasText("Test select action")
                        }
                    }
                }
            }

        }
    }

    @Test
    fun loadsNextPassageWhenSelectingSingleSelectAction() {
        MockWebServer().use { webServer ->
            webServer.start(8080)

            webServer.enqueue(
                MockResponse().setBody(
                    """{"data":{"embarkStory":{"__typename":"EmbarkStory","startPassage":"1","passages":[{"__typename":"EmbarkPassage","name":"TestPassage","id":"1","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"123"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"NEVER","text":"Unary false test"}],"text":"Unary false test"},{"__typename":"EmbarkMessage","expressions":[{"__typename":"EmbarkExpressionUnary","unaryType":"ALWAYS","text":"Unary true test"}],"text":"Unary true test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage2","label":"Test select action"},"keys":["FOO"],"values":["BAR"]}]}}},{"__typename":"EmbarkPassage","name":"TestPassage2","id":"2","messages":[{"__typename":"EmbarkMessage","expressions":[],"text":"another test message"},{"__typename":"EmbarkMessage","expressions":[],"text":"456"},{"__typename":"EmbarkMessage","expressions":[],"text":"{FOO} test"}],"action":{"__typename":"EmbarkSelectAction","data":{"__typename":"EmbarkSelectActionData","options":[{"__typename":"EmbarkSelectActionOption","link":{"__typename":"EmbarkLink","name":"TestPassage","label":"Another test select action"},"keys":[],"values":[]}]}}}]}}}"""
                )
            )

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            onScreen<EmbarkScreen> {
                selectActions {
                    firstChild<EmbarkScreen.SelectAction> {
                        click()
                    }
                }
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text {
                            hasText("another test message")
                        }
                    }
                    childAt<EmbarkScreen.MessageRow>(1) {
                        text {
                            hasText("456")
                        }
                    }
                }
                selectActions {
                    firstChild<EmbarkScreen.SelectAction> {
                        button {
                            hasText("Another test select action")
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val INTENT_WITH_STORY_NAME = Intent().apply {
            putExtra(EmbarkActivity.STORY_NAME, "example")
        }
    }
}
