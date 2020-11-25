package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class EmbarkStoryDataBuilder(
    private val name: String = "Embark story name",
    private val startPassage: String = "1",
    private val passages: List<EmbarkStoryQuery.Passage> = emptyList()
) {
    fun build() = EmbarkStoryQuery.Data(
        embarkStory = EmbarkStoryQuery.EmbarkStory(
            startPassage = startPassage,
            passages = passages
        )
    )
}
