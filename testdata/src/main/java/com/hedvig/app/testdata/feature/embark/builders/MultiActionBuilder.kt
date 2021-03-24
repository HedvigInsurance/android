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
        asEmbarkMultiAction = EmbarkStoryQuery.AsEmbarkMultiAction(
            data = EmbarkStoryQuery.Data5(
                components = listOf(
                    EmbarkStoryQuery.Component(
                        asEmbarkDropdownAction = EmbarkStoryQuery.AsEmbarkDropdownAction(
                            data = EmbarkStoryQuery.Data7(
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
                            data = EmbarkStoryQuery.Data8(
                                label = "Water connected",
                                key = "water",
                                defaultValue = true
                            )
                        ),
                        asEmbarkNumberAction1 = EmbarkStoryQuery.AsEmbarkNumberAction1(
                            data = EmbarkStoryQuery.Data6(
                                label = "Size",
                                key = "size",
                                placeholder = "Size",
                                unit = "sqm",
                                minValue = 10,
                                maxValue = 10000,
                                link = EmbarkStoryQuery.Link4(fragments = EmbarkStoryQuery.Link4.Fragments(embarkLinkFragment = link))
                            )
                        )
                    )
                )
            )
        ),
    )
}
