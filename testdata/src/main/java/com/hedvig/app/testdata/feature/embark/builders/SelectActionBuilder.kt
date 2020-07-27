package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class SelectActionBuilder(
    private val options: List<EmbarkStoryQuery.Option> = emptyList()
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
            data = EmbarkStoryQuery.Data1(
                options = options
            )
        ),
        asEmbarkTextAction = null
    )
}

data class SelectOptionBuilder(
    private val link: EmbarkLinkFragment,
    private val keyValues: List<Pair<String, String>> = emptyList()
) {
    fun build() = EmbarkStoryQuery.Option(
        link = EmbarkStoryQuery.Link(
            name = link.name,
            label = link.label
        ),
        keys = keyValues.map { it.first },
        values = keyValues.map { it.second }
    )
}
