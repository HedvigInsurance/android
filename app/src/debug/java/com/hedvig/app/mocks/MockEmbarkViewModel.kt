package com.hedvig.app.mocks

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeUnary
import com.hedvig.app.feature.embark.EmbarkViewModel

class MockEmbarkViewModel : EmbarkViewModel() {
    override fun load(name: String) {
        storyData = EmbarkStoryQuery.Data(
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
                            ),
                            EmbarkStoryQuery.Message(
                                text = "123",
                                expressions = emptyList()
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Unary false test",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        asEmbarkExpressionUnary = EmbarkStoryQuery.AsEmbarkExpressionUnary(
                                            type = EmbarkExpressionTypeUnary.NEVER,
                                            text = "Unary false test"
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Unary true test",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        asEmbarkExpressionUnary = EmbarkStoryQuery.AsEmbarkExpressionUnary(
                                            type = EmbarkExpressionTypeUnary.ALWAYS,
                                            text = "Unary true test"
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
                                                name = "TestPassage2",
                                                label = "Test select action"
                                            ),
                                            key = "FOO",
                                            value = "BAR"
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    EmbarkStoryQuery.Passage(
                        name = "TestPassage2",
                        id = "2",
                        messages = listOf(
                            EmbarkStoryQuery.Message(
                                text = "another test message",
                                expressions = emptyList()
                            ),
                            EmbarkStoryQuery.Message(
                                text = "456",
                                expressions = emptyList()
                            ),
                            EmbarkStoryQuery.Message(
                                text = "{FOO} test",
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
                                            key = null,
                                            value = null
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        )
        displayInitialPassage()
    }
}
