package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.EmbarkLinkFragment

data class DatePickerActionBuilder(
    val key: String = "BAR",
    val label: String = "",
    val link: EmbarkLinkFragment,
) {
    fun build() = EmbarkStoryQuery.Action(
        __typename = "",
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = EmbarkStoryQuery.AsEmbarkDatePickerAction(
            __typename = "",
            storeKey = key,
            label = label,
            next = EmbarkStoryQuery.Next2(
                __typename = "",
                fragments = EmbarkStoryQuery.Next2.Fragments(link)
            )
        ),
        asEmbarkMultiAction = null,
        asEmbarkAudioRecorderAction = null,
        asEmbarkExternalInsuranceProviderAction = null,
        asEmbarkAddressAutocompleteAction = null,
    )
}
