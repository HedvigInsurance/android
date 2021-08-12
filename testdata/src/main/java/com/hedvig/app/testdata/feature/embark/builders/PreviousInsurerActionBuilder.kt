package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

data class PreviousInsurerActionBuilder(
    val storeKey: String = "BAR",
    val next: EmbarkLinkFragment,
) {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction(
            previousInsurerData = EmbarkStoryQuery.PreviousInsurerData(
                insuranceProviders = listOf(
                    EmbarkStoryQuery.InsuranceProvider(
                        id = "if",
                        name = "IF",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(svgUrl = "/app-content-service/if.svg")
                                    )
                                )
                            )
                        )
                    ),
                    EmbarkStoryQuery.InsuranceProvider(
                        id = "trygghansa",
                        name = "Trygg-Hansa",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(
                                            svgUrl = "/app-content-service/trygg_hansa.svg"
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    EmbarkStoryQuery.InsuranceProvider(
                        id = "folksam",
                        name = "Folksam",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(svgUrl = "/app-content-service/folksam.svg")
                                    )
                                )
                            )
                        )
                    )
                ),
                storeKey = storeKey,
                next = EmbarkStoryQuery.Next(fragments = EmbarkStoryQuery.Next.Fragments(next)),
                skip = EmbarkStoryQuery.Skip(
                    fragments = EmbarkStoryQuery.Skip.Fragments(
                        EmbarkLinkFragment(
                            name = "test_skip",
                            label = "Skip"
                        )
                    )
                ),
            )
        ),
        asEmbarkNumberAction = null,
        asEmbarkNumberActionSet = null,
        asEmbarkDatePickerAction = null,
        asEmbarkMultiAction = null,
    )
}
