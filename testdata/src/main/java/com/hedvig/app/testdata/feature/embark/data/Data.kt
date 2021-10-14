package com.hedvig.app.testdata.feature.embark.data

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.GraphQLErrorsFragment
import com.hedvig.android.owldroid.fragment.GraphQLResultsFragment
import com.hedvig.android.owldroid.fragment.MessageFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLSingleVariableCasting
import com.hedvig.android.owldroid.type.EmbarkAPIGraphQLVariableGeneratedType
import com.hedvig.android.owldroid.type.EmbarkExternalRedirectLocation
import com.hedvig.app.testdata.feature.embark.builders.DatePickerActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.ExpressionBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLApiBuilder
import com.hedvig.app.testdata.feature.embark.builders.GraphQLVariableBuilder
import com.hedvig.app.testdata.feature.embark.builders.GroupedResponseBuilder
import com.hedvig.app.testdata.feature.embark.builders.MessageBuilder
import com.hedvig.app.testdata.feature.embark.builders.MultiActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.NumberActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.NumberActionSetBuilder
import com.hedvig.app.testdata.feature.embark.builders.PassageBuilder
import com.hedvig.app.testdata.feature.embark.builders.PreviousInsurerActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.RedirectBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectOptionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder.Companion.EMAIL
import com.hedvig.app.testdata.feature.embark.builders.TextActionBuilder.Companion.PERSONAL_NUMBER
import com.hedvig.app.testdata.feature.embark.builders.TextActionSetBuilder
import com.hedvig.app.testdata.feature.embark.builders.TrackBuilder
import org.json.JSONObject

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
        ).buildMessageResponse(),
        messages = listOf(
            STANDARD_FIRST_MESSAGE
        ),
        action = SelectActionBuilder(
            listOf(
                SelectOptionBuilder(
                    link = STANDARD_FIRST_LINK
                ).build(),
                SelectOptionBuilder(
                    link = STANDARD_SECOND_LINK
                ).build(),
                SelectOptionBuilder(
                    link = STANDARD_FIRST_LINK
                ).build(),
                SelectOptionBuilder(
                    link = STANDARD_SECOND_LINK
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
                ).build(),
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

val STORY_WITH_SINGLE_TOOLTIP = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            tooltip = listOf(
                EmbarkStoryQuery.Tooltip(
                    title = "Number of co-insured",
                    description = "E.g. partner, children or roomies that should be covered." +
                        " Co-insured must live together with you to be covered by your insurance"
                )
            )
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_FOUR_TOOLTIP = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            tooltip = listOf(
                EmbarkStoryQuery.Tooltip(
                    title = "Apartment",
                    description = "For those of you who rent or own an apartment"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "House",
                    description = "For those of you who live in a detached" +
                        " or terraced house. We don't insure holiday homes"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Student housing",
                    description = "For those of you who rent a student apartment or a room in a student corridor"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Renting a room",
                    description = "For those of you who rent one or several rooms in an apartment or house"
                )
            )
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_MANY_TOOLTIP = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            tooltip = listOf(
                EmbarkStoryQuery.Tooltip(
                    title = "Apartment",
                    description = "For those of you who rent or own an apartment"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "House",
                    description = "For those of you who live in a detached" +
                        " or terraced house. We don't insure holiday homes"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Student housing",
                    description = "For those of you who rent a student apartment or a room in a student corridor"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Renting a room",
                    description = "For those of you who rent one or several rooms in an apartment or house"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Apartment",
                    description = "For those of you who rent or own an apartment"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "House",
                    description = "For those of you who live in a" +
                        " detached or terraced house. We don't insure holiday homes"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Student housing",
                    description = "For those of you who rent a student apartment or a room in a student corridor"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Renting a room",
                    description = "For those of you who rent one or several rooms in an apartment or house"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Apartment",
                    description = "For those of you who rent or own an apartment"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "House",
                    description = "For those of you who live in a" +
                        " detached or terraced house. We don't insure holiday homes"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Student housing",
                    description = "For those of you who rent a student apartment or a room in a student corridor"
                ),
                EmbarkStoryQuery.Tooltip(
                    title = "Renting a room",
                    description = "For those of you who rent one or several rooms in an apartment or house"
                ),
            )
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()

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

val STORY_WITH_TEXT_ACTION_EMAIL_VALIDATION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "Test hint",
                mask = TextActionBuilder.EMAIL
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

val STORY_WITH_TEXT_ACTION_PERSONAL_NUMBER = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "970407-1234",
                mask = TextActionBuilder.PERSONAL_NUMBER
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

val STORY_WITH_TEXT_ACTION_BIRTH_DATE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "9999-99-99",
                mask = TextActionBuilder.BIRTH_DATE
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

val STORY_WITH_TEXT_ACTION_BIRTH_DATE_REVERSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "99-99-9999",
                mask = TextActionBuilder.BIRTH_DATE_REVERSE
            ).build()
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{BAR} was entered. {BAR.Age} was derived.")
                    .build(),
            )
        ).build()
    )
).build()

val STORY_WITH_TEXT_ACTION_NORWEGIAN_POSTAL_CODE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "9999",
                mask = TextActionBuilder.NORWEGIAN_POSTAL_CODE
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

val STORY_WITH_TEXT_ACTION_SWEDISH_POSTAL_CODE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionBuilder(
                key = "BAR",
                link = STANDARD_FIRST_LINK,
                placeholder = "999 99",
                mask = TextActionBuilder.SWEDISH_POSTAL_CODE
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

val PREVIOUS_INSURER_STORY = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = PreviousInsurerActionBuilder(
                storeKey = "BAR",
                next = STANDARD_FIRST_LINK
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

val STORY_WITH_TEXT_ACTION_SET = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionSetBuilder(
                link = STANDARD_FIRST_LINK,
                textActions = listOf(
                    TextActionBuilder(
                        placeholder = "Placeholder",
                        title = "Hint",
                        key = "FOO",
                        mask = null
                    ).buildTextActionSetAction(),
                    TextActionBuilder(
                        placeholder = "Second Placeholder",
                        title = "Second Hint",
                        key = "BAR",
                        mask = null
                    ).buildTextActionSetAction(),
                )
            ).build()
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{FOO} {BAR} was entered")
                    .build()
            )
        ).build()
    )
).build()

val STORY_WITH_TEXT_ACTION_SET_FIRST_TEXT_PERSONAL_NUMBER_SECOND_TEXT_EMAIL_VALIDATION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = TextActionSetBuilder(
                link = STANDARD_FIRST_LINK,
                textActions = listOf(
                    TextActionBuilder(
                        placeholder = "901124-1234",
                        title = "Personal number",
                        key = "FOO",
                        mask = PERSONAL_NUMBER
                    ).buildTextActionSetAction(),
                    TextActionBuilder(
                        placeholder = "example@email.com",
                        title = "Email",
                        key = "BAR",
                        mask = EMAIL
                    ).buildTextActionSetAction(),
                )
            ).build()
        ).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{FOO} {BAR} was entered")
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
                    asEmbarkTextAction = null,
                    asEmbarkTextActionSet = null,
                    asEmbarkPreviousInsuranceProviderAction = null,
                    asEmbarkNumberAction = null,
                    asEmbarkNumberActionSet = null,
                    asEmbarkDatePickerAction = null,
                    asEmbarkMultiAction = null,
                    asEmbarkAudioRecorderAction = null,
                )
            )
            .build()
    )
).build()

val STORY_WITH_TEXT_ACTION_AND_CUSTOM_RESPONSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                response = MessageBuilder(text = "{BAR} response").buildMessageResponse(),
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
                response = MessageBuilder(text = "{FOO} response").buildMessageResponse(),
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
                        text = "Unary true test",
                        expressions = listOf(
                            ExpressionBuilder(
                                type = ExpressionBuilder.ExpressionType.ALWAYS,
                                text = "Unary true test"
                            ).build()
                        )
                    ).build(),
                    MessageBuilder(
                        text = "Unary false test",
                        expressions = listOf(
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
                                    ).build(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).build()
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
                                    ).build(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).build()
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
                                    ).build(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.ALWAYS
                                    ).build()
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
                                    ).build(),
                                    ExpressionBuilder(
                                        type = ExpressionBuilder.ExpressionType.NEVER
                                    ).build()
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
                                ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build(),
                                ExpressionBuilder(type = ExpressionBuilder.ExpressionType.ALWAYS).build(),
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
                    type = GraphQLApiBuilder.Type.QUERY,
                    query = HELLO_QUERY,
                    results = listOf(
                        GraphQLResultsFragment(
                            key = "hello", as_ = "HELLO"
                        )
                    ),
                    errors = listOf(
                        GraphQLErrorsFragment(
                            contains = null,
                            next = GraphQLErrorsFragment.Next(
                                fragments = GraphQLErrorsFragment.Next.Fragments(
                                    LINK_TO_FOURTH_PASSAGE
                                )
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
                    type = GraphQLApiBuilder.Type.QUERY,
                    query = VARIABLE_QUERY,
                    results = listOf(
                        GraphQLResultsFragment(key = "hello", as_ = "VARIABLE")
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
                    type = GraphQLApiBuilder.Type.QUERY,
                    query = VARIABLE_QUERY,
                    results = listOf(
                        GraphQLResultsFragment(key = "hello", as_ = "VARIABLE")
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

val HELLO_MUTATION = """
mutation {
    hello
}
""".trimIndent()

val STORY_WITH_GRAPHQL_MUTATION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                api = GraphQLApiBuilder(
                    type = GraphQLApiBuilder.Type.MUTATION,
                    query = HELLO_MUTATION,
                    results = listOf(
                        GraphQLResultsFragment(
                            key = "hello", as_ = "HELLO"
                        )
                    ),
                    errors = listOf(
                        GraphQLErrorsFragment(

                            contains = null,
                            next = GraphQLErrorsFragment.Next(
                                fragments = GraphQLErrorsFragment.Next.Fragments(
                                    LINK_TO_FOURTH_PASSAGE
                                )
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

val VARIABLE_MUTATION = """
mutation VariableMutation(${'$'}variable: String!) {
    hello(variable: ${'$'}variable)
}
""".trimIndent()

val STORY_WITH_GRAPHQL_MUTATION_AND_SINGLE_VARIABLE = EmbarkStoryDataBuilder(
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
                    type = GraphQLApiBuilder.Type.MUTATION,
                    query = VARIABLE_MUTATION,
                    results = listOf(
                        GraphQLResultsFragment(
                            key = "hello", as_ = "VARIABLE"
                        )
                    ),
                    errors = listOf(
                        GraphQLErrorsFragment(
                            contains = null,
                            next = GraphQLErrorsFragment.Next(
                                fragments = GraphQLErrorsFragment.Next.Fragments(
                                    LINK_TO_FOURTH_PASSAGE
                                )
                            )
                        )
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

val STORY_WITH_NUMBER_ACTION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = NumberActionBuilder(
                    unit = "other people",
                    placeholder = "1",
                    label = "Co-insured",
                    maxValue = 75,
                    minValue = 1,
                    link = STANDARD_FIRST_LINK,
                ).build(),
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{BAR} was entered")
                    .build()
            )
        ).build()
    )
).build()

val STORY_WITH_NUMBER_ACTION_AND_CUSTOM_RESPONSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = NumberActionBuilder(
                    unit = "other people",
                    placeholder = "1",
                    label = "Co-insured",
                    maxValue = 75,
                    minValue = 1,
                    link = STANDARD_FIRST_LINK,
                ).build(),
                response = MessageBuilder("custom response").buildExpressionResponse(),
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{BAR} was entered")
                    .build()
            )
        ).build()
    )
).build()

val STORY_WITH_NUMBER_ACTION_SET = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = NumberActionSetBuilder(
                    listOf(
                        NumberActionSetBuilder.NumberAction(
                            key = "BAR",
                            unit = "other people",
                            placeholder = "1",
                            label = "",
                            maxValue = 75,
                            minValue = 1,
                            title = "Co-insured",
                        ),
                        NumberActionSetBuilder.NumberAction(
                            key = "FOO",
                            unit = "sqm",
                            placeholder = "52",
                            label = "",
                            maxValue = 75,
                            minValue = 1,
                            title = "House size",
                        )
                    ),
                    link = STANDARD_FIRST_LINK
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            messages = listOf(
                MessageBuilder("{BAR} was entered")
                    .build()
            )
        ).build()
    )
).build()

val STORY_WITCH_DATE_PICKER = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(
            action = DatePickerActionBuilder(
                label = "Move in date",
                key = "BAR",
                link = STANDARD_FIRST_LINK
            ).build()
        ).build()
    )
).build()

val STORY_WITH_TRACK = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = STANDARD_FIRST_LINK,
                            keyValues = listOf(
                                "FOO" to "BAR",
                                "BAZ" to "BAT",
                            )
                        ).build()
                    )
                ).build(),
                tracks = listOf(
                    TrackBuilder("Enter Passage").build()
                )
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            link = LINK_TO_THIRD_PASSAGE,
                        ).build()
                    )
                ).build(),
                tracks = listOf(
                    TrackBuilder("Enter second passage", includeAllKeys = true).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(
                tracks = listOf(
                    TrackBuilder(
                        "Enter third passage",
                        keys = listOf("FOO"),
                        customData = JSONObject("{\"CUSTOM\": \"DATA\"}")
                    ).build()
                )
            )
            .build()
    )
).build()

val STORY_WITH_OFFER_REDIRECT = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(offerRedirectKeys = listOf("123", "123"))
            .build()
    )
).build()

val STORY_WITH_COMPUTED_VALUE = EmbarkStoryDataBuilder(
    computedStoreValues = listOf(
        EmbarkStoryQuery.ComputedStoreValue(key = "BAR", value = "FOO + 3")
    ),
    passages = listOf(
        PassageBuilder(
            name = "TestPassage",
            id = "1",
            response = MessageBuilder(
                text = "{TestPassageResult}"
            ).buildMessageResponse(),
            messages = listOf(
                MessageFragment(
                    text = "Text on input in next passage will have added 3 to your input",
                    expressions = emptyList()
                )
            ),
            action = NumberActionBuilder(
                "FOO",
                link = STANDARD_FIRST_LINK
            ).build()
        ).build(),
        PassageBuilder(
            name = "TestPassage2",
            id = "2",
            response = MessageBuilder(
                text = "{TestPassageResult}"
            ).buildMessageResponse(),
            messages = listOf(
                MessageBuilder(
                    text = "Computed value is previous input + 3 = {BAR}"
                ).build()
            ),
            action = TextActionBuilder(
                link = STANDARD_SECOND_LINK,
                key = "BAR"
            ).build()
        ).build(),
    )
).build()

val STORY_FOR_STORE_VERSIONING = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(link = STANDARD_FIRST_LINK, keyValues = listOf("FOO" to "BAR")).build(),
                        SelectOptionBuilder(link = STANDARD_FIRST_LINK).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                redirects = listOf(
                    RedirectBuilder(
                        to = LINK_TO_THIRD_PASSAGE.name,
                        expression = ExpressionBuilder(
                            ExpressionBuilder.ExpressionType.EQUALS,
                            key = "FOO",
                            value = "BAR"
                        ).build()
                    ).build()
                )
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER.build()
    )
).build()

val STORY_WITH_MULTI_ACTION = EmbarkStoryDataBuilder(
    passages = listOf(
        PassageBuilder(
            name = "TestPassage",
            id = "1",
            response = GroupedResponseBuilder(
                title = "Test title",
                each = "FOO" to MessageBuilder(
                    text = "Building: {Building}, size: {size}, water connected: {water}"
                ).build()
            ).build(),
            messages = listOf(
                MessageFragment(
                    text = "OK. We need some information about any extra buildings on the site," +
                        "if you want them to be covered by the insurance",
                    expressions = emptyList()
                ),
                MessageFragment(
                    text = "If you don't want to insure any extra buildings, just press Continue",
                    expressions = emptyList()
                )
            ),
            action = MultiActionBuilder(
                "FOO",
                link = STANDARD_FIRST_LINK
            ).build()
        ).build(),
        PassageBuilder(
            name = "TestPassage2",
            id = "2",
            response = MessageBuilder(
                text = "{TestPassageResult}"
            ).buildMessageResponse(),
            messages = listOf(
                MessageBuilder(
                    text = "Computed value is previous input + 3 = {BAR}"
                ).build()
            ),
            action = TextActionBuilder(
                link = STANDARD_SECOND_LINK,
                key = "BAR"
            ).build()
        ).build(),
    )
).build()

val STORY_WITH_MARKDOWN_MESSAGE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                messages = listOf(
                    MessageBuilder(text = "*Hello* **world** ~strikethrough~").build(),
                    MessageBuilder(text = "[link](https://www.example.com)").build(),
                    MessageBuilder(text = "# heading").build(),
                    MessageBuilder(text = ">quote").build(),
                    MessageBuilder(text = "`code`").build(),
                    MessageBuilder(text = "1. one\n2. two").build(),
                    MessageBuilder(text = "- one\n- two").build(),
                )
            )
            .build()
    )
).build()

val STORY_WITH_GRAPHQL_MUTATION_AND_OFFER_REDIRECT = EmbarkStoryDataBuilder(
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
                    type = GraphQLApiBuilder.Type.MUTATION,
                    query = VARIABLE_MUTATION,
                    results = listOf(
                        GraphQLResultsFragment(
                            key = "hello", as_ = "VARIABLE"
                        )
                    ),
                    errors = listOf(
                        GraphQLErrorsFragment(
                            contains = null,
                            next = GraphQLErrorsFragment.Next(
                                fragments = GraphQLErrorsFragment.Next.Fragments(
                                    LINK_TO_FOURTH_PASSAGE
                                )
                            )
                        )
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
                offerRedirectKeys = listOf("VARIABLE"),
                messages = listOf(
                    MessageBuilder("api result: {VARIABLE}").build()
                )
            )
            .build(),
        STANDARD_FOURTH_PASSAGE_BUILDER
            .build()
    )
).build()

val STORY_WITH_CLOSE_AND_CHAT = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(link = STANDARD_FIRST_LINK.copy(label = "Chat")).build(),
                        SelectOptionBuilder(link = LINK_TO_THIRD_PASSAGE.copy(label = "Close")).build(),
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .copy(
                externalRedirect = EmbarkExternalRedirectLocation.CHAT
            )
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy(externalRedirect = EmbarkExternalRedirectLocation.CLOSE)
            .build()
    )
).build()

val STORY_WITH_TEXT_ACTION_SET_API = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = TextActionSetBuilder(
                    api = GraphQLApiBuilder(
                        type = GraphQLApiBuilder.Type.QUERY,
                        query = HELLO_QUERY,
                        results = listOf(
                            GraphQLResultsFragment(
                                key = "hello", as_ = "HELLO"
                            )
                        ),
                        errors = listOf(
                            GraphQLErrorsFragment(
                                contains = null,
                                next = GraphQLErrorsFragment.Next(
                                    fragments = GraphQLErrorsFragment.Next.Fragments(
                                        LINK_TO_FOURTH_PASSAGE
                                    )
                                )
                            )
                        ),
                        next = LINK_TO_THIRD_PASSAGE
                    ).build(),
                    link = STANDARD_SECOND_LINK,
                    textActions = listOf(
                        TextActionBuilder(
                            key = "",
                        ).buildTextActionSetAction()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_TEXT_ACTION_API = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = TextActionBuilder(
                    api = GraphQLApiBuilder(
                        type = GraphQLApiBuilder.Type.QUERY,
                        query = HELLO_QUERY,
                        results = listOf(
                            GraphQLResultsFragment(
                                key = "hello", as_ = "HELLO"
                            )
                        ),
                        errors = listOf(
                            GraphQLErrorsFragment(
                                contains = null,
                                next = GraphQLErrorsFragment.Next(
                                    fragments = GraphQLErrorsFragment.Next.Fragments(
                                        LINK_TO_FOURTH_PASSAGE
                                    )
                                )
                            )
                        ),
                        next = LINK_TO_THIRD_PASSAGE
                    ).build(),
                    link = STANDARD_SECOND_LINK,
                    key = "",
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_SELECT_ACTION_API_SINGLE_OPTION = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            api = GraphQLApiBuilder(
                                type = GraphQLApiBuilder.Type.QUERY,
                                query = HELLO_QUERY,
                                results = listOf(
                                    GraphQLResultsFragment(
                                        key = "hello", as_ = "HELLO"
                                    )
                                ),
                                errors = listOf(
                                    GraphQLErrorsFragment(
                                        contains = null,
                                        next = GraphQLErrorsFragment.Next(
                                            fragments = GraphQLErrorsFragment.Next.Fragments(
                                                LINK_TO_FOURTH_PASSAGE
                                            )
                                        )
                                    )
                                ),
                                next = LINK_TO_THIRD_PASSAGE
                            ).build(),
                            link = STANDARD_SECOND_LINK
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()

val STORY_WITH_SELECT_ACTION_API_MULTIPLE_OPTIONS = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                action = SelectActionBuilder(
                    options = listOf(
                        SelectOptionBuilder(
                            api = GraphQLApiBuilder(
                                type = GraphQLApiBuilder.Type.QUERY,
                                query = HELLO_QUERY,
                                results = listOf(
                                    GraphQLResultsFragment(
                                        key = "hello", as_ = "HELLO"
                                    )
                                ),
                                errors = listOf(
                                    GraphQLErrorsFragment(
                                        contains = null,
                                        next = GraphQLErrorsFragment.Next(
                                            fragments = GraphQLErrorsFragment.Next.Fragments(
                                                LINK_TO_FOURTH_PASSAGE
                                            )
                                        )
                                    )
                                ),
                                next = LINK_TO_FOURTH_PASSAGE
                            ).build(),
                            link = STANDARD_SECOND_LINK
                        ).build(),
                        SelectOptionBuilder(
                            api = GraphQLApiBuilder(
                                type = GraphQLApiBuilder.Type.QUERY,
                                query = HELLO_QUERY,
                                results = listOf(
                                    GraphQLResultsFragment(
                                        key = "hello", as_ = "HELLO"
                                    )
                                ),
                                errors = listOf(
                                    GraphQLErrorsFragment(
                                        contains = null,
                                        next = GraphQLErrorsFragment.Next(
                                            fragments = GraphQLErrorsFragment.Next.Fragments(
                                                LINK_TO_FOURTH_PASSAGE
                                            )
                                        )
                                    )
                                ),
                                next = LINK_TO_THIRD_PASSAGE
                            ).build(),
                            link = STANDARD_SECOND_LINK
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER
            .build(),
        STANDARD_THIRD_PASSAGE_BUILDER
            .copy()
            .build()
    )
).build()
