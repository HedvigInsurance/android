package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

object PreviousInsurerAction {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction(
            data = EmbarkStoryQuery.Data5(
                insuranceProviders = listOf(
                    EmbarkStoryQuery.InsuranceProvider(
                        name = "IF",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(svgUrl = "/app-content-service/if.svg"))
                                )
                            )
                        )
                    ),
                    EmbarkStoryQuery.InsuranceProvider(
                        name = "Trygg-Hansa",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(svgUrl = "/app-content-service/trygg_hansa.svg"))
                                )
                            )
                        )
                    ),
                    EmbarkStoryQuery.InsuranceProvider(
                        name = "Folksam",
                        logo = EmbarkStoryQuery.Logo(
                            variants = EmbarkStoryQuery.Variants(
                                fragments = EmbarkStoryQuery.Variants.Fragments(
                                    iconVariantsFragment = IconVariantsFragment(
                                        dark = IconVariantsFragment.Dark(svgUrl = ""),
                                        light = IconVariantsFragment.Light(svgUrl = "/app-content-service/folksam.svg"))
                                )
                            )
                        )
                    )
                ),
                storeKey = "123",
                next = EmbarkStoryQuery.Next(fragments = EmbarkStoryQuery.Next.Fragments(EmbarkLinkFragment(name = "test_next", label = "Next"))),
                skip = EmbarkStoryQuery.Skip(fragments = EmbarkStoryQuery.Skip.Fragments(EmbarkLinkFragment(name = "test_skip", label = "Skip"))),
            )
        )
    )
}
