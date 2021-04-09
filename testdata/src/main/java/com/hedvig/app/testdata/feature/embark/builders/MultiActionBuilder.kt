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
            data = EmbarkStoryQuery.Data4(
                components = listOf(
                    EmbarkStoryQuery.Component(
                        asEmbarkDropdownAction = EmbarkStoryQuery.AsEmbarkDropdownAction(
                            data = EmbarkStoryQuery.Data6(
                                label = "Building type",
                                key = "Building",
                                options = listOf(
                                    EmbarkStoryQuery.Option1(
                                        value = "Garage",
                                        text = "Garage"
                                    ),
                                    EmbarkStoryQuery.Option1(
                                        value = "Attefall",
                                        text = "Attefall",
                                    ),
                                    EmbarkStoryQuery.Option1(
                                        value = "Friggebod",
                                        text = "Friggebod"
                                    )
                                )
                            )
                        ),
                        asEmbarkSwitchAction = EmbarkStoryQuery.AsEmbarkSwitchAction(
                            data = EmbarkStoryQuery.Data7(
                                label = "Water connected",
                                key = "water",
                                defaultValue = true
                            )
                        ),
                        asEmbarkNumberAction1 = EmbarkStoryQuery.AsEmbarkNumberAction1(
                            data = EmbarkStoryQuery.Data5(
                                fragments = EmbarkStoryQuery.Data5.Fragments(
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
                        )
                    )
                )
            )
        ),
    )
}
