package com.hedvig.app.mocks

import com.hedvig.android.owldroid.fragment.ExpressionFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkExpressionTypeBinary
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
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = ExpressionFragment.AsEmbarkExpressionUnary(
                                                    unaryType = EmbarkExpressionTypeUnary.NEVER,
                                                    text = "Unary false test"
                                                ),
                                                asEmbarkExpressionBinary = null,
                                                asEmbarkExpressionMultiple = null
                                            )
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Unary true test",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = ExpressionFragment.AsEmbarkExpressionUnary(
                                                    unaryType = EmbarkExpressionTypeUnary.ALWAYS,
                                                    text = "Unary true test"
                                                ),
                                                asEmbarkExpressionBinary = null,
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
                            ),
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
                                                    key = "BOO",
                                                    value = "FAR",
                                                    text = "Binary equals test message that evaluates to false"
                                                ),
                                                asEmbarkExpressionMultiple = null
                                            )
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Binary greater than test message that evaluates to true",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = ExpressionFragment.AsEmbarkExpressionBinary(
                                                    binaryType = EmbarkExpressionTypeBinary.MORE_THAN,
                                                    key = "BAZ",
                                                    value = "4",
                                                    text = "Binary greater than test message that evaluates to true"
                                                ),
                                                asEmbarkExpressionMultiple = null
                                            )
                                        )
                                    )
                                )
                            ),
                            EmbarkStoryQuery.Message(
                                text = "Binary greater than test message that evaluates to false",
                                expressions = listOf(
                                    EmbarkStoryQuery.Expression(
                                        fragments = EmbarkStoryQuery.Expression.Fragments(
                                            ExpressionFragment(
                                                asEmbarkExpressionUnary = null,
                                                asEmbarkExpressionBinary = ExpressionFragment.AsEmbarkExpressionBinary(
                                                    binaryType = EmbarkExpressionTypeBinary.MORE_THAN,
                                                    key = "BAZ",
                                                    value = "6",
                                                    text = "Binary greater than test message that evaluates to false"
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
                            ),
                            asEmbarkTextAction = null
                        ),
                        redirects = emptyList()
                    )
                )
            )
        )
        displayInitialPassage()
    }
}
