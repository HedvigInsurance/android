package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.EmbarkNumberActionFragment
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
                        asEmbarkNumberAction1 = null
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
                        asEmbarkNumberAction1 = null
                    ),
                    EmbarkStoryQuery.Component(
                        asEmbarkNumberAction1 = EmbarkStoryQuery.AsEmbarkNumberAction1(
                            numberActionData = EmbarkStoryQuery.NumberActionData1(
                                fragments = EmbarkStoryQuery.NumberActionData1.Fragments(
                                    EmbarkNumberActionFragment(
                                        label = "Size",
                                        key = "size",
                                        placeholder = "Size",
                                        unit = "sqm",
                                        minValue = 10,
                                        maxValue = 10000,
                                        link = EmbarkNumberActionFragment.Link(
                                            fragments = EmbarkNumberActionFragment.Link.Fragments(
                                                embarkLinkFragment = link
                                            )
                                        )
                                    )
                                )
                            )
                        ),
                        asEmbarkDropdownAction = null,
                        asEmbarkSwitchAction = null
                    )
                )
            )
        ),
    )
}
