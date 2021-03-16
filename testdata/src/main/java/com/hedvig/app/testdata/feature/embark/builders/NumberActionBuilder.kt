package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

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
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = EmbarkStoryQuery.AsEmbarkNumberAction(
            data = EmbarkStoryQuery.Data4(
                key = key,
                placeholder = placeholder,
                unit = unit,
                label = label,
                maxValue = maxValue,
                minValue = minValue,
                link = EmbarkStoryQuery.Link3(fragments = EmbarkStoryQuery.Link3.Fragments(embarkLinkFragment = link))
            )
        ),
        asEmbarkDatePickerAction = null,
    )
}
