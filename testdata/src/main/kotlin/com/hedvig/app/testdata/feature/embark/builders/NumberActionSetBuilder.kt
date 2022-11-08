package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.apollo.graphql.EmbarkStoryQuery
import com.hedvig.android.apollo.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.apollo.graphql.type.EmbarkNumberActionSet

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
    __typename = EmbarkNumberActionSet.type.name,
    asEmbarkSelectAction = null,
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = null,
    asEmbarkPreviousInsuranceProviderAction = null,
    asEmbarkNumberAction = null,
    asEmbarkMultiAction = null,
    asEmbarkNumberActionSet = EmbarkStoryQuery.AsEmbarkNumberActionSet(
      __typename = EmbarkNumberActionSet.type.name,
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
              title = it.title,
            ),
          )
        },
        link = EmbarkStoryQuery.Link3(
          __typename = "",
          fragments = EmbarkStoryQuery.Link3.Fragments(
            embarkLinkFragment = link,
          ),
        ),
      ),
    ),
    asEmbarkDatePickerAction = null,
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}
