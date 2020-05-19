package com.hedvig.app.feature.embark.messages

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen.Companion.onScreen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.fragment.SubExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeMultiple
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.app.feature.embark.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.awaitility.Duration.TWO_SECONDS
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndExpressionTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun shouldShowMessageForWhenWithAndExpression() {
        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(MockResponse().setBody(DATA.toJson()))

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            onScreen<EmbarkScreen> {
                selectActions { firstChild<EmbarkScreen.SelectAction> { click() } }
                await atMost TWO_SECONDS untilAsserted {
                    messages {
                        hasSize(1)
                        firstChild<EmbarkScreen.MessageRow> {
                            text { hasText("And test message that evaluates to true") }
                        }
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
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "test message",
                                        expressions = emptyList()
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "test message",
                                        expressions = emptyList()
                                    )
                                )
                            )
                        ),
                        response = EmbarkStoryQuery.Response(
                            fragments = EmbarkStoryQuery.Response.Fragments(
                                messageFragment = null
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
                                            keys = listOf("FOO", "BAZ"),
                                            values = listOf("BAR", "5")
                                        )
                                    )
                                )
                            ),
                            asEmbarkTextAction = null
                        ),
                        redirects = emptyList()
                    ),
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage2",
                        id = "2",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "And test message that evaluates to true",
                                        expressions = listOf(
                                            MessageFragment.Expression(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = null,
                                                asEmbarkExpressionMultiple = MessageFragment.AsEmbarkExpressionMultiple(
                                                    multipleType = EmbarkExpressionTypeMultiple.AND,
                                                    subExpressions = listOf(
                                                        MessageFragment.SubExpression(
                                                            fragments = MessageFragment.SubExpression.Fragments(
                                                                SubExpressionFragment(
                                                                    asEmbarkExpressionUnary = SubExpressionFragment.AsEmbarkExpressionUnary(
                                                                        unaryType = EmbarkExpressionTypeUnary.ALWAYS,
                                                                        text = null
                                                                    ),
                                                                    asEmbarkExpressionBinary = null,
                                                                    asEmbarkExpressionMultiple = null
                                                                )
                                                            )
                                                        ),
                                                        MessageFragment.SubExpression(
                                                            fragments = MessageFragment.SubExpression.Fragments(
                                                                SubExpressionFragment(
                                                                    asEmbarkExpressionUnary = SubExpressionFragment.AsEmbarkExpressionUnary(
                                                                        unaryType = EmbarkExpressionTypeUnary.ALWAYS,
                                                                        text = null
                                                                    ),
                                                                    asEmbarkExpressionBinary = null,
                                                                    asEmbarkExpressionMultiple = null
                                                                )
                                                            )
                                                        )
                                                    ),
                                                    text = "And test message that evaluates to true"
                                                )
                                            )
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                fragments = EmbarkStoryQuery.Message.Fragments(
                                    MessageFragment(
                                        text = "And test message that evaluates to false",
                                        expressions = listOf(
                                            MessageFragment.Expression(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = null,
                                                asEmbarkExpressionMultiple = MessageFragment.AsEmbarkExpressionMultiple(
                                                    multipleType = EmbarkExpressionTypeMultiple.AND,
                                                    subExpressions = listOf(
                                                        MessageFragment.SubExpression(
                                                            fragments = MessageFragment.SubExpression.Fragments(
                                                                SubExpressionFragment(
                                                                    asEmbarkExpressionUnary = SubExpressionFragment.AsEmbarkExpressionUnary(
                                                                        unaryType = EmbarkExpressionTypeUnary.NEVER,
                                                                        text = null
                                                                    ),
                                                                    asEmbarkExpressionBinary = null,
                                                                    asEmbarkExpressionMultiple = null
                                                                )
                                                            )
                                                        ),
                                                        MessageFragment.SubExpression(
                                                            fragments = MessageFragment.SubExpression.Fragments(
                                                                SubExpressionFragment(
                                                                    asEmbarkExpressionUnary = SubExpressionFragment.AsEmbarkExpressionUnary(
                                                                        unaryType = EmbarkExpressionTypeUnary.ALWAYS,
                                                                        text = null
                                                                    ),
                                                                    asEmbarkExpressionBinary = null,
                                                                    asEmbarkExpressionMultiple = null
                                                                )
                                                            )
                                                        )
                                                    ),
                                                    text = "And test message that evaluates to false"
                                                )
                                            )
                                        )

                                    )
                                )
                            )
                        ),
                        response = EmbarkStoryQuery.Response(
                            fragments = EmbarkStoryQuery.Response.Fragments(
                                messageFragment = null
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
                            ),
                            asEmbarkTextAction = null
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
