package com.hedvig.app.testdata.feature.embark.builders

import giraffe.EmbarkStoryQuery
import giraffe.fragment.EmbarkLinkFragment
import giraffe.type.EmbarkDropdownAction
import giraffe.type.EmbarkMultiAction
import giraffe.type.EmbarkMultiActionNumberAction
import giraffe.type.EmbarkSwitchAction

data class MultiActionBuilder(
  private val key: String,
  private val link: EmbarkLinkFragment,
) {
  fun build() = EmbarkStoryQuery.Action(
    __typename = EmbarkMultiAction.type.name,
    asEmbarkSelectAction = null,
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = null,
    asEmbarkPreviousInsuranceProviderAction = null,
    asEmbarkNumberAction = null,
    asEmbarkDatePickerAction = null,
    asEmbarkNumberActionSet = null,
    asEmbarkMultiAction = EmbarkStoryQuery.AsEmbarkMultiAction(
      __typename = EmbarkMultiAction.type.name,
      multiActionData = EmbarkStoryQuery.MultiActionData(
        key = key,
        addLabel = "Continue",
        maxAmount = "2",
        link = EmbarkStoryQuery.Link4(
          __typename = "",
          fragments = EmbarkStoryQuery.Link4.Fragments(
            embarkLinkFragment = link,
          ),
        ),
        components = listOf(
          EmbarkStoryQuery.Component(
            __typename = EmbarkDropdownAction.type.name,
            asEmbarkDropdownAction = EmbarkStoryQuery.AsEmbarkDropdownAction(
              __typename = EmbarkDropdownAction.type.name,
              dropDownActionData = EmbarkStoryQuery.DropDownActionData(
                label = "Building type",
                key = "Building",
                options = listOf(
                  EmbarkStoryQuery.Option1(
                    value = "GarageVal",
                    text = "Garage",
                  ),
                  EmbarkStoryQuery.Option1(
                    value = "AttefallVal",
                    text = "Attefall",
                  ),
                  EmbarkStoryQuery.Option1(
                    value = "FriggebodVal",
                    text = "Friggebod",
                  ),
                ),
              ),
            ),
            asEmbarkSwitchAction = null,
            asEmbarkMultiActionNumberAction = null,
          ),
          EmbarkStoryQuery.Component(
            __typename = EmbarkMultiActionNumberAction.type.name,
            asEmbarkMultiActionNumberAction = EmbarkStoryQuery.AsEmbarkMultiActionNumberAction(
              __typename = EmbarkMultiActionNumberAction.type.name,
              numberActionData = EmbarkStoryQuery.NumberActionData1(
                key = "size",
                placeholder = "52",
                label = "Size",
                unit = "sqm",
              ),
            ),
            asEmbarkDropdownAction = null,
            asEmbarkSwitchAction = null,
          ),
          EmbarkStoryQuery.Component(
            __typename = EmbarkSwitchAction.type.name,
            asEmbarkSwitchAction = EmbarkStoryQuery.AsEmbarkSwitchAction(
              __typename = EmbarkSwitchAction.type.name,
              switchActionData = EmbarkStoryQuery.SwitchActionData(
                label = "Water connected",
                key = "water",
                defaultValue = true,
              ),
            ),
            asEmbarkDropdownAction = null,
            asEmbarkMultiActionNumberAction = null,
          ),
        ),
      ),
    ),
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}
