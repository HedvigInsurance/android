package com.hedvig.app.testdata.feature.embark.data

import com.hedvig.app.testdata.feature.embark.builders.EmbarkStoryDataBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectActionBuilder
import com.hedvig.app.testdata.feature.embark.builders.SelectOptionBuilder

val PROGRESSABLE_STORY = EmbarkStoryDataBuilder(
    passages = listOf(
        STANDARD_FIRST_PASSAGE_BUILDER.copy(links = listOf(
            STANDARD_FIRST_LINK
        )).build(),
        STANDARD_SECOND_PASSAGE_BUILDER.copy(
            action = SelectActionBuilder(
                options = listOf(SelectOptionBuilder(LINK_TO_THIRD_PASSAGE).build())
            ).build(),
            links = listOf(LINK_TO_THIRD_PASSAGE)
        ).build(),
        STANDARD_THIRD_PASSAGE_BUILDER.copy(
            action = SelectActionBuilder(options = listOf(SelectOptionBuilder(LINK_TO_FOURTH_PASSAGE).build())).build(),
            links = listOf(LINK_TO_FOURTH_PASSAGE)
        ).build()
    )
).build()
