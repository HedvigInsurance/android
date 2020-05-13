package com.hedvig.app.feature.embark.messages

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.agoda.kakao.screen.Screen
import com.apollographql.apollo.api.toJson
import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
import com.hedvig.app.feature.embark.EmbarkActivity
import com.hedvig.app.feature.embark.screens.EmbarkScreen
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EqualsExpressionTest {
    @get:Rule
    val activityRule = ActivityTestRule(EmbarkActivity::class.java, false, false)

    @Test
    fun shouldShowMessageForWhenWithEqualsExpression() {
        MockWebServer().use { webServer ->
            webServer.start(8080)
            webServer.enqueue(
                MockResponse().setBody(DATA.toJson())
            )

            activityRule.launchActivity(INTENT_WITH_STORY_NAME)

            Screen.onScreen<EmbarkScreen> {
                selectActions {
                    firstChild<EmbarkScreen.SelectAction> { click() }
                }
                messages {
                    hasSize(1)
                    firstChild<EmbarkScreen.MessageRow> {
                        text {
                            hasText("Binary equals test message that evaluates to true")
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
                                text = "Binary equals test message that evaluates to true",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = ExpressionFragment.AsEmbarkExpressionBinary(
                                                    binaryType = EmbarkExpressionTypeBinary.EQUALS,
                                                    key = "FOO",
                                                    value = "BAR",
                                                    text = "Binary equals test message that evaluates to true"
                                                ),
                                                asEmbarkExpressionMultiple = null
                                            )
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Binary equals test message that evaluates to false",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = ExpressionFragment.AsEmbarkExpressionBinary(
                                                    binaryType = EmbarkExpressionTypeBinary.EQUALS,
                                                    key = "BAZ",
                                                    value = "4",
                                                    text = "Binary equals test message that evaluates to false"
                                                ),
                                                asEmbarkExpressionMultiple = null
                                            )
                                        )
                                    )
                                )
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
