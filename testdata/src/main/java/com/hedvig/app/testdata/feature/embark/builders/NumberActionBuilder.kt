package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.fragment.EmbarkNumberActionFragment

data class NumberActionBuilder(
    val key: String = "BAR",
    val placeholder: String = "",
    val unit: String? = null,
    val label: String? = null,
    val maxValue: Int? = null,
    val minValue: Int? = null,
    val link: EmbarkLinkFragment,
) {
    fun build() = EmbarkStoryQuery.Action(
        __typename = "",
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = EmbarkStoryQuery.AsEmbarkNumberAction(
            __typename = "",
            numberActionData = EmbarkStoryQuery.NumberActionData(
                __typename = "",
                fragments = EmbarkStoryQuery.NumberActionData.Fragments(
                    embarkNumberActionFragment = EmbarkNumberActionFragment(
                        key = key,
                        placeholder = placeholder,
                        unit = unit,
                        label = label,
                        maxValue = maxValue,
                        minValue = minValue,
                        link = EmbarkNumberActionFragment.Link(
                            __typename = "",
                            fragments = EmbarkNumberActionFragment.Link.Fragments(
                                embarkLinkFragment = link
                            )
                        )
                    )
                )
            )
        ),
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = null,
        asEmbarkMultiAction = null,
        asEmbarkAudioRecorderAction = null,
        asEmbarkExternalInsuranceProviderAction = null,
        asEmbarkAddressAutocompleteAction = null,
    )
}
