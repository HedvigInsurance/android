package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class TextActionSetBuilder(
    private val firstKey: String,
    private val secondKey: String,
    private val link: EmbarkLinkFragment,
    private val firstPlaceholder: String = "First Placeholder",
    private val secondPlaceholder: String = "Second Placeholder"
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = EmbarkStoryQuery.AsEmbarkTextActionSet(
            data = EmbarkStoryQuery.Data3(
                link = EmbarkStoryQuery.Link2(fragments = EmbarkStoryQuery.Link2.Fragments(link)),
                textActions = listOf(
                    EmbarkStoryQuery.TextAction(
                        data = EmbarkStoryQuery.Data4(
                            placeholder = firstPlaceholder,
                            key = firstKey
                        )
                    ),
                    EmbarkStoryQuery.TextAction(
                        data = EmbarkStoryQuery.Data4(
                            placeholder = secondPlaceholder,
                            key = secondKey
                        )
                    ),
                )
            )
        )
    )
}

