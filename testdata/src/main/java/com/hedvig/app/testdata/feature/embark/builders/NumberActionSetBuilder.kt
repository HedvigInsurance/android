package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class NumberActionSetBuilder(
    val numberActions: List<NumberAction>,
    val link: EmbarkLinkFragment,
) {

    data class NumberAction(
        val key: String = "BAR",
        val placeholder: String = "",
        val unit: String? = null,
        val label: String? = null,
        val maxValue: Int? = null,
        val minValue: Int? = null,
        val title: String,
    )

    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkMultiAction = null,
        asEmbarkNumberActionSet = EmbarkStoryQuery.AsEmbarkNumberActionSet(
            numberActionSetData = EmbarkStoryQuery.NumberActionSetData(
                numberActions = numberActions.map {
                    EmbarkStoryQuery.NumberAction(
                        data = EmbarkStoryQuery.Data4(
                            key = it.key,
                            placeholder = it.placeholder,
                            unit = it.unit,
                            label = it.label,
                            maxValue = it.maxValue,
                            minValue = it.minValue,
                            title = it.title
                        )
                    )
                },
                link = EmbarkStoryQuery.Link3(
                    fragments = EmbarkStoryQuery.Link3.Fragments(
                        embarkLinkFragment = link
                    )
                ),
            )
        ),
        asEmbarkDatePickerAction = null,
        asEmbarkAudioRecorderAction = null,
    )
}
