package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.ApiFragment
import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class SelectActionBuilder(
    private val options: List<EmbarkStoryQuery.Option> = emptyList()
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
            selectData = EmbarkStoryQuery.SelectData(
                options = options
            )
        ),
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = null,
        asEmbarkMultiAction = null,
    )
}

data class SelectOptionBuilder(
    private val link: EmbarkLinkFragment,
    private val keyValues: List<Pair<String, String>> = emptyList(),
    private val api: ApiFragment? = null,
) {
    fun build() = EmbarkStoryQuery.Option(
        link = EmbarkStoryQuery.Link(
            fragments = EmbarkStoryQuery.Link.Fragments(link)
        ),
        keys = keyValues.map { it.first },
        values = keyValues.map { it.second },
        api = api?.let { EmbarkStoryQuery.Api(fragments = EmbarkStoryQuery.Api.Fragments(it)) },
    )
}
