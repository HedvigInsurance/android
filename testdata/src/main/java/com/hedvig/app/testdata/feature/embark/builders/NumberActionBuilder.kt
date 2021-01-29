package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class NumberActionBuilder(
    val key: String = "BAR",
    val placeholder: String = "",
    val link: EmbarkLinkFragment,
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkNumberAction = EmbarkStoryQuery.AsEmbarkNumberAction(
            data = EmbarkStoryQuery.Data2(
                key = key,
                placeholder = placeholder,
                link = EmbarkStoryQuery.Link3(fragments = EmbarkStoryQuery.Link3.Fragments(embarkLinkFragment = link))
            )
        ),
    )
}
