package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class MultiActionBuilder(
    private val key: String,
    private val link: EmbarkLinkFragment
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = null,
        asEmbarkNumberAction = null,
        asEmbarkDatePickerAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkMultiAction = EmbarkStoryQuery.AsEmbarkMultiAction(
            multiActionData = EmbarkStoryQuery.MultiActionData(
                key = "testkey",
                addLabel = "Continue",
                maxAmount = "2",
                link = EmbarkStoryQuery.Link4(
                    fragments = EmbarkStoryQuery.Link4.Fragments(
                        embarkLinkFragment = link
                    )
                ),
                components = listOf(
                    EmbarkStoryQuery.Component(
                        asEmbarkDropdownAction = EmbarkStoryQuery.AsEmbarkDropdownAction(
                            dropDownActionData = EmbarkStoryQuery.DropDownActionData(
                                label = "Building type",
                                key = "Building",
                                options = listOf(
                                    EmbarkStoryQuery.Option1(
                                        value = "GarageVal",
                                        text = "Garage"
                                    ),
                                    EmbarkStoryQuery.Option1(
                                        value = "AttefallVal",
                                        text = "Attefall",
                                    ),
                                    EmbarkStoryQuery.Option1(
                                        value = "FriggebodVal",
                                        text = "Friggebod"
                                    )
                                )
                            )
                        ),
                        asEmbarkSwitchAction = null,
                        asEmbarkMultiActionNumberAction = null
                    ),
                    EmbarkStoryQuery.Component(
                        asEmbarkMultiActionNumberAction = EmbarkStoryQuery.AsEmbarkMultiActionNumberAction(
                            numberActionData = EmbarkStoryQuery.NumberActionData1(
                                key = "size",
                                placeholder = "52",
                                label = "Size",
                                unit = "sqm",
                            )
                        ),
                        asEmbarkDropdownAction = null,
                        asEmbarkSwitchAction = null
                    ),
                    EmbarkStoryQuery.Component(
                        asEmbarkSwitchAction = EmbarkStoryQuery.AsEmbarkSwitchAction(
                            switchActionData = EmbarkStoryQuery.SwitchActionData(
                                label = "Water connected",
                                key = "water",
                                defaultValue = true
                            )
                        ),
                        asEmbarkDropdownAction = null,
                        asEmbarkMultiActionNumberAction = null
                    ),
                )
            )
        ),
    )
}
