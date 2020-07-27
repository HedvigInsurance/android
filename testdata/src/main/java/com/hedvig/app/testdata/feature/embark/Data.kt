package com.hedvig.app.testdata.feature.embark

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
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
