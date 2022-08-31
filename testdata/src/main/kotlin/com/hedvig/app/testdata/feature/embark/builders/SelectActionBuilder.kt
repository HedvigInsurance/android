package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import com.hedvig.android.apollo.graphql.fragment.ApiFragment
import com.hedvig.android.apollo.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.apollo.graphql.type.EmbarkLink
import com.hedvig.android.apollo.graphql.type.EmbarkSelectAction

data class SelectActionBuilder(
  private val options: List<EmbarkStoryQuery.Option> = emptyList(),
) {
  fun build() = EmbarkStoryQuery.Action(
    __typename = EmbarkSelectAction.type.name,
    asEmbarkSelectAction = EmbarkStoryQuery.AsEmbarkSelectAction(
      __typename = EmbarkSelectAction.type.name,
      selectData = EmbarkStoryQuery.SelectData(
        options = options,
      ),
    ),
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = null,
    asEmbarkPreviousInsuranceProviderAction = null,
    asEmbarkNumberAction = null,
    asEmbarkNumberActionSet = null,
    asEmbarkDatePickerAction = null,
    asEmbarkMultiAction = null,
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}

data class SelectOptionBuilder(
  private val link: EmbarkLinkFragment,
  private val keyValues: List<Pair<String, String>> = emptyList(),
  private val api: ApiFragment? = null,
  private val badge: String? = null,
) {
  fun build() = EmbarkStoryQuery.Option(
    link = EmbarkStoryQuery.Link(
      __typename = EmbarkLink.type.name,
      fragments = EmbarkStoryQuery.Link.Fragments(link),
    ),
    keys = keyValues.map { it.first },
    values = keyValues.map { it.second },
    badge = badge,
    api = api?.let {
      EmbarkStoryQuery.Api(
        __typename = it.__typename,
        fragments = EmbarkStoryQuery.Api.Fragments(it),
      )
    },
  )
}
