package com.hedvig.app.feature.embark.redirects

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.app.feature.embark.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnaryExpressionRedirectTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun shouldRedirectOnPassageWithRedirect() {
        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            onScreen<EmbarkScreen> {
                selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
                messages {
                    firstChild<EmbarkScreen.MessageRow> {
                        text { hasText("a third test message") }
                    }
                }
            }
        }
    }

    companion object {
        private val DATA = EmbarkStoryQuery.Data(
            embarkStory = EmbarkStoryQuery.EmbarkStory(
                startPassage = "1",
                passages = listOf(
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage",
                        id = "1",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                text = "test message",
                                expressions = emptyList()
                            )
                        ),
                        action = EmbarkStoryQuery.Action(
                            asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
                                data = EmbarkStoryQuery.Data1(
                                    options = listOf(
                                        EmbarkStoryQuery.Option(
                                            link = EmbarkStoryQuery.Link(
                                                name = "TestPassage2",
                                                label = "Test select action"
                                            ),
                                            keys = listOf("FOO"),
                                            values = listOf("BAR")
                                        )
                                    )
                                )
                            )
                        ),
                        redirects = emptyList()
                    ),
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage2",
                        id = "2",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                text = "another test message",
                                expressions = emptyList()
                            )
                        ),
                        action = EmbarkStoryQuery.Action(
                            asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
                                data = EmbarkStoryQuery.Data1(
                                    options = listOf(
                                        EmbarkStoryQuery.Option(
                                            link = EmbarkStoryQuery.Link(
                                                name = "TestPassage",
                                                label = "Another test select action"
                                            ),
                                            keys = emptyList(),
                                            values = emptyList()
                                        )
                                    )
                                )
                            )
                        ),
                        redirects = listOf(
                            EmbarkStoryQuery.Redirect(
                                asEmbarkRedirectUnaryExpression = EmbarkStoryQuery.AsEmbarkRedirectUnaryExpression(
                                    unaryType = EmbarkExpressionTypeUnary.ALWAYS,
                                    to = "TestPassage3",
                                    passedExpressionKey = null,
                                    passedExpressionValue = null
                                ),
                                asEmbarkRedirectBinaryExpression = null,
                                asEmbarkRedirectMultipleExpressions = null
                            )
                        )
                    ),
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage3",
                        id = "3",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                text = "a third test message",
                                expressions = emptyList()
                            )
                        ),
                        action = EmbarkStoryQuery.Action(
                            asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
                                data = EmbarkStoryQuery.Data1(
                                    options = listOf(
                                        EmbarkStoryQuery.Option(
                                            link = EmbarkStoryQuery.Link(
                                                name = "TestPassage",
                                                label = "A third test select action"
                                            ),
                                            keys = emptyList(),
                                            values = emptyList()
                                        )
                                    )
                                )
                            )
                        ),
                        redirects = emptyList()
                    )
                )
            )
        )
        private val INTENT_WITH_STORY_NAME = Intent().apply {
            putExtra(EmbarkActivity.STORY_NAME, this@Companion::class.java.name)
        }
    }
}
