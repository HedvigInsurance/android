package com.hedvig.app.testdata.feature.embark

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.ExpressionBuilder
import com.hedvig.app.testdata.feature.embark.builders.MessageBuilder
import com.hedvig.app.testdata.feature.embark.builders.PassageBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectOptionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder

val STANDARD_FIRST_MESSAGE = MessageBuilder(
    text = "test message"
).build()
val STANDARD_SECOND_MESSAGE = MessageBuilder(
    text = "another test message"
).build()

val STANDARD_FIRST_LINK = EmbarkLinkFragment(
    name = "TestPassage2",
    label = "Another test passage"
)

val STANDARD_SECOND_LINK = EmbarkLinkFragment(
    name = "TestPassage",
    label = "Yet another test passage"
)

val STANDARD_FIRST_PASSAGE_BUILDER =
    PassageBuilder(
        name = "TestPassage",
        id = "1",
        response = MessageBuilder(
            text = "{TestPassageResult}"
        ).build(),
        messages = listOf(
            STANDARD_FIRST_MESSAGE
        ),
        action = SelectActionBuilder(
            listOf(
                SelectOptionBuilder(
                    link = STANDARD_FIRST_LINK
                ).build()
            )
        ).build()
    )

val STANDARD_SECOND_PASSAGE_BUILDER =
    PassageBuilder(
        name = "TestPassage2",
        id = "2",
        messages = listOf(STANDARD_SECOND_MESSAGE),
        action = SelectActionBuilder(
            listOf(
                SelectOptionBuilder(
                    link = STANDARD_SECOND_LINK
                ).build()
            )
        ).build()
    )

val STANDARD_STORY = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_TEXT_ACTION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "Test hint"
            ).build()
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{BAR} was entered")
                    .build()
            )
        ).build()
    )
).build()

val STORY_WITH_INCOMPATIBLE_ACTION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = EmbarkStoryQuery.Action(
                    asEmbarkSelectAction = null,
                    asEmbarkTextAction = null
                )
            )
            .build()
    )
).build()

val STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                response = MessageBuilder(text = "{BAR} response").build(),
                action = TextActionBuilder(
                    key = "BAR", link = STANDARD_FIRST_LINK
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_SELECT_ACTION_AND_CUSTOM_RESPONSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                response = MessageBuilder(text = "{FOO} response").build(),
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = STANDARD_FIRST_LINK,
                            keyValues = listOf("FOO" to "BAR")
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_UNARY_EXPRESSIONS = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "Unary true test", expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.ALWAYS,
                                text = "Unary true test"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Unary false test", expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.NEVER,
                                text = "Unary false test"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_EQUALS_EXPRESSION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = STANDARD_FIRST_LINK,
                            keyValues = listOf("FOO" to "BAR")
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "Binary equals test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.EQUALS,
                                text = "Binary equals test message that evaluates to true",
                                key = "FOO",
                                value = "BAR"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Binary equals test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.EQUALS,
                                text = "Binary equals test message that evaluates to false",
                                key = "BAZ",
                                value = "4"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_NOT_EQUALS_EXPRESSION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = STANDARD_FIRST_LINK,
                            keyValues = listOf(
                                "FOO" to "BAR",
                                "BAZ" to "5"
                            )
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "Not equals test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
                                text = "Not equals test message that evaluates to true",
                                key = "FOO",
                                value = "BAZ"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Not equals test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.NOT_EQUALS,
                                text = "Not equals test message that evaluates to false",
                                key = "BAZ",
                                value = "5"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_GREATER_THAN_EXPRESSION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = STANDARD_FIRST_LINK,
                            keyValues = listOf("FOO" to "5")
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "Binary greater than test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.GREATER_THAN,
                                text = "Binary greater than test message that evaluates to true",
                                key = "FOO",
                                value = "4"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Binary greater than test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.GREATER_THAN,
                                text = "Binary greater than test message that evaluates to false",
                                key = "FOO",
                                value = "6"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()
