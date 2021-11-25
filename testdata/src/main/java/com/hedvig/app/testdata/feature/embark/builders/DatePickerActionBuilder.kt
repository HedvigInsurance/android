package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class DatePickerActionBuilder(
    val key: String = "BAR",
    val label: String = "",
    val link: EmbarkLinkFragment,
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = EmbarkStoryQuery.AsEmbarkDatePickerAction(
            storeKey = key,
            label = label,
            next = EmbarkStoryQuery.Next2(fragments = EmbarkStoryQuery.Next2.Fragments(link))
        ),
        asEmbarkMultiAction = null,
        asEmbarkAudioRecorderAction = null,
        asEmbarkExternalInsuranceProviderAction = null,
    )
}
