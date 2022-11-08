package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import com.hedvig.android.apollo.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.apollo.graphql.type.EmbarkDatePickerAction

data class DatePickerActionBuilder(
  val key: String = "BAR",
  val label: String = "",
  val link: EmbarkLinkFragment,
) {
  fun build() = EmbarkStoryQuery.Action(
    __typename = EmbarkDatePickerAction.type.name,
    asEmbarkSelectAction = null,
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = null,
    asEmbarkPreviousInsuranceProviderAction = null,
    asEmbarkNumberAction = null,
    asEmbarkNumberActionSet = null,
    asEmbarkDatePickerAction = EmbarkStoryQuery.AsEmbarkDatePickerAction(
      __typename = EmbarkDatePickerAction.type.name,
      storeKey = key,
      label = label,
      next = EmbarkStoryQuery.Next2(
        __typename = "",
        fragments = EmbarkStoryQuery.Next2.Fragments(link),
      ),
    ),
    asEmbarkMultiAction = null,
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}
