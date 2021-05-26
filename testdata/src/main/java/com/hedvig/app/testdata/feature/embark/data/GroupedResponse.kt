package com.hedvig.app.testdata.feature.embark.data

import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.GroupedResponseBuilder
import com.hedvig.app.testdata.feature.embark.builders.MessageBuilder

val STORY_WITH_GROUPED_RESPONSE = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER
            .copy(
                response = GroupedResponseBuilder(
                    title = "Test title",
                    items = listOf(
                        MessageBuilder(
                            "Test message 1"
                        ).build(),
                        MessageBuilder(
                            "Test message 2"
                        ).build()
                    )
                ).build()
            )
            .build(),
        STANDARD_SECOND_PASSAGE_BUILDER.build()
    )
).build()
