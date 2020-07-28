package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class TextActionBuilder(
    private val key: String,
    private val link: EmbarkLinkFragment,
    private val placeholder: String = ""
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = EmbarkStoryQuery.AsEmbarkTextAction(
            data = EmbarkStoryQuery.Data2(
                key = key,
                link = EmbarkStoryQuery.Link1(
                    fragments = EmbarkStoryQuery.Link1.Fragments(link)
                ),
                placeholder = placeholder
            )
        )
    )
}
