package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class TextActionSetBuilder(
    private val link: EmbarkLinkFragment,
    private val textActions: List<EmbarkStoryQuery.TextAction>,
    private val api: ApiFragment? = null
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = EmbarkStoryQuery.AsEmbarkTextActionSet(
            textSetData = EmbarkStoryQuery.TextSetData(
                link = EmbarkStoryQuery.Link2(fragments = EmbarkStoryQuery.Link2.Fragments(link)),
                textActions = textActions,
                api = api?.let { EmbarkStoryQuery.Api2(fragments = EmbarkStoryQuery.Api2.Fragments(it)) },
            )
        ),
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = null,
        asEmbarkMultiAction = null,
        asEmbarkAudioRecorderAction = null,
        asEmbarkExternalInsuranceProviderAction = null,
    )
}
