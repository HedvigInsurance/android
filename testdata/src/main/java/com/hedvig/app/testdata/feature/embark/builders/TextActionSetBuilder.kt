package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class TextActionSetBuilder(
    private val link: EmbarkLinkFragment,
    private val textActions: List<EmbarkStoryQuery.TextAction>,
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = EmbarkStoryQuery.AsEmbarkTextActionSet(
            textSetData = EmbarkStoryQuery.TextSetData(
                link = EmbarkStoryQuery.Link2(fragments = EmbarkStoryQuery.Link2.Fragments(link)),
                textActions = textActions
            )
        ),
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = null,
        asEmbarkMultiAction = null,
    )
}
