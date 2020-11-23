package com.hedvig.app.testdata.feature.embark

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLVariableGeneratedType
import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.ExpressionBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLApiBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLVariableBuilder
import com.hedvig.app.testdata.feature.embark.builders.MessageBuilder
import com.hedvig.app.testdata.feature.embark.builders.PassageBuilder
import com.hedvig.app.testdata.feature.embark.builders.RedirectBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectOptionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder

val STANDARD_FIRST_MESSAGE = MessageBuilder(
    text = "test message"
).build()
val STANDARD_SECOND_MESSAGE = MessageBuilder(
    text = "another test message"
).build()
val STANDARD_THIRD_MESSAGE = MessageBuilder(
    text = "a third message"
).build()
val STANDARD_FOURTH_MESSAGE = MessageBuilder(
    text = "a fourth message"
).build()

val STANDARD_FIRST_LINK = EmbarkLinkFragment(
    name = "TestPassage2",
    label = "Another test passage"
)

val STANDARD_SECOND_LINK = EmbarkLinkFragment(
    name = "TestPassage",
    label = "Yet another test passage"
)

val LINK_TO_THIRD_PASSAGE = EmbarkLinkFragment(
    name = "TestPassage3",
    label = "A third test passage"
)

val LINK_TO_FOURTH_PASSAGE = EmbarkLinkFragment(
    name = "TestPassage4",
    label = "A fourth test passage"
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

val STANDARD_THIRD_PASSAGE_BUILDER =
    PassageBuilder(
        name = "TestPassage3",
        id = "3",
        messages = listOf(STANDARD_THIRD_MESSAGE),
        action = SelectActionBuilder(
            listOf(
                SelectOptionBuilder(
                    link = STANDARD_SECOND_LINK
                ).build()
            )
        ).build()
    )

val STANDARD_FOURTH_PASSAGE_BUILDER = PassageBuilder(
    name = "TestPassage4",
    id = "4",
    messages = listOf(STANDARD_FOURTH_MESSAGE),
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

val STORY_WITH_GREATER_THAN_OR_EQUALS_EXPRESSION = EmbarkStoryDataBuilder(
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
                        text = "Binary greater than or equals test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.GREATER_THAN_OR_EQUALS,
                                text = "Binary greater than or equals test message that evaluates to true",
                                key = "FOO",
                                value = "5"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Binary greater than or equals test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.GREATER_THAN_OR_EQUALS,
                                text = "Binary greater than or equals test message that evaluates to false",
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

val STORY_WITH_LESS_THAN_EXPRESSION = EmbarkStoryDataBuilder(
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
                        text = "Less than test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.LESS_THAN,
                                text = "Less than test message that evaluates to true",
                                key = "FOO",
                                value = "6"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Less than test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.LESS_THAN,
                                text = "Less than test message that evaluates to true",
                                key = "FOO",
                                value = "4"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_OR_EXPRESSION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "Or test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.OR,
                                text = "Or test message that evaluates to true",
                                subExpressions = listOf(
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.ALWAYS
                                    ).buildSubExpression(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).buildSubExpression()
                                )
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Or test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.OR,
                                text = "Or test message that evaluates to false",
                                subExpressions = listOf(
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).buildSubExpression(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).buildSubExpression()
                                )
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_AND_EXPRESSION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "And test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.AND,
                                text = "And test message that evaluates to true",
                                subExpressions = listOf(
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.ALWAYS
                                    ).buildSubExpression(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.ALWAYS
                                    ).buildSubExpression()
                                )
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "And test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.AND,
                                text = "And test message that evaluates to false",
                                subExpressions = listOf(
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.ALWAYS
                                    ).buildSubExpression(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).buildSubExpression()
                                )
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_LESS_THAN_OR_EQUALS_EXPRESSION = EmbarkStoryDataBuilder(
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
                        text = "Less than or equals test message that evaluates to true",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.LESS_THAN_OR_EQUALS,
                                text = "Less than or equals test message that evaluates to true",
                                key = "FOO",
                                value = "5"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Less than or equals test message that evaluates to false",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.LESS_THAN_OR_EQUALS,
                                text = "Less than or equals test message that evaluates to false",
                                key = "FOO",
                                value = "4"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_TEMPLATE_MESSAGE = EmbarkStoryDataBuilder(
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
                    MessageBuilder(text = "{FOO} test").build()
                )
            ).build()
    )
).build()

val STORY_WITH_UNARY_REDIRECT = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy()
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                redirects = listOf(
                    RedirectBuilder(
                        to = "TestPassage3",
                        expression = ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build()
                    ).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_BINARY_REDIRECT = EmbarkStoryDataBuilder(
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
                redirects = listOf(
                    RedirectBuilder(
                        to = "TestPassage3",
                        expression = ExpressionBuilder(
                            type = ExpressionBuilder.ExpressionType.EQUALS,
                            key = "FOO",
                            value = "BAR"
                        ).build()
                    ).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_MULTIPLE_REDIRECTS = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                redirects = listOf(
                    RedirectBuilder(
                        to = "TestPassage3",
                        expression = ExpressionBuilder(
                            type = ExpressionBuilder.ExpressionType.AND,
                            subExpressions = listOf(
                                ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).buildSubExpression(),
                                ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).buildSubExpression()
                            )
                        ).build()
                    ).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_PASSED_KEY_VALUE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy()
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                redirects = listOf(
                    RedirectBuilder(
                        to = "TestPassage3",
                        expression = ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build(),
                        passedExpressionKey = "FOO",
                        passedExpressionValue = "BAR"
                    ).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(
                        text = "conditionally shown message",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.EQUALS,
                                key = "FOO",
                                value = "BAR",
                                text = "conditionally shown message"
                            ).build()
                        )
                    ).build()
                )
            )
            .build()
    )
).build()

const val HELLO_QUERY = """
{
    hello
}
"""

val STORY_WITH_GRAPHQL_QUERY_API = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                api = GraphQLApiBuilder(
                    query = HELLO_QUERY,
                    results = listOf(
                        ApiFragment.Result(key = "hello", as_ = "HELLO")
                    ),
                    errors = listOf(
                        ApiFragment.Error(
                            contains = null,
                            next = ApiFragment.Next(
                                fragments = ApiFragment.Next.Fragments(LINK_TO_FOURTH_PASSAGE)
                            )
                        )
                    ),
                    next = LINK_TO_THIRD_PASSAGE
                ).build()
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder("api result: {HELLO}").build()
                )
            )
            .build(),
        STANDARD_FOURTH_PASSAGE_BUILDER
            .build()
    )
).build()

val VARIABLE_QUERY = """
query VariableQuery(${'$'}variable: String!) {
    hello(variable: ${'$'}variable)
}
"""

val STORY_WITH_GRAPHQL_QUERY_API_AND_SINGLE_VARIABLE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = TextActionBuilder(
                    key = "input",
                    link = STANDARD_FIRST_LINK
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                api = GraphQLApiBuilder(
                    query = VARIABLE_QUERY,
                    results = listOf(
                        ApiFragment.Result(key = "hello", as_ = "VARIABLE")
                    ),
                    variables = listOf(
                        GraphQLVariableBuilder(
                            kind = GraphQLVariableBuilder.VariableKind.SINGLE,
                            key = "variable",
                            from = "input",
                            singleType = EmbarkAPIGraphQLSingleVariableCasting.STRING
                        ).build()
                    ),
                    next = LINK_TO_THIRD_PASSAGE
                ).build()
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder("api result: {VARIABLE}").build()
                )
            )
            .build(),
        STANDARD_FOURTH_PASSAGE_BUILDER
            .build()
    )
).build()

val STORY_WITH_GRAPHQL_QUERY_API_AND_GENERATED_VARIABLE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                api = GraphQLApiBuilder(
                    query = VARIABLE_QUERY,
                    results = listOf(
                        ApiFragment.Result(key = "hello", as_ = "VARIABLE")
                    ),
                    variables = listOf(
                        GraphQLVariableBuilder(
                            kind = GraphQLVariableBuilder.VariableKind.GENERATED,
                            key = "variable",
                            storeAs = "STORED",
                            generatedType = EmbarkAPIGraphQLVariableGeneratedType.UUID
                        ).build()
                    ),
                    next = LINK_TO_THIRD_PASSAGE
                ).build()
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(text = "api result: {VARIABLE}").build(),
                    MessageBuilder(text = "stored: {STORED}").build()
                )
            )
            .build(),
        STANDARD_FOURTH_PASSAGE_BUILDER
            .build()
    )
).build()

val STORY_WITH_TOOLTIP = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                tooltip = listOf(
                    EmbarkStoryQuery.Tooltip(
                        title = "Tooltip Title",
                        description = "Tooltip Description"
                    )
                )
            )
            .build()
    )
).build()
