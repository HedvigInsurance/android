package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery

object PreviousInsurerAction {
    fun build() = EmbarkStoryQuery.Action(
        asEmbarkSelectAction = null,
        asEmbarkTextAction = null,
        asEmbarkTextActionSet = null,
        asEmbarkPreviousInsuranceProviderAction = EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction(data = EmbarkStoryQuery.Data5(insuranceProviders = listOf(
            EmbarkStoryQuery.InsuranceProvider(name = "IF", logo = EmbarkStoryQuery.Logo(variants = EmbarkStoryQuery.Variants(
                fragments = EmbarkStoryQuery.Variants.Fragments(
                    iconVariantsFragment = IconVariantsFragment(dark = IconVariantsFragment.Dark(svgUrl = ""), light = IconVariantsFragment.Light(svgUrl = ""))
                )
            ))),
            EmbarkStoryQuery.InsuranceProvider(name = "Trygg-hansa", logo = EmbarkStoryQuery.Logo(variants = EmbarkStoryQuery.Variants(
                fragments = EmbarkStoryQuery.Variants.Fragments(
                    iconVariantsFragment = IconVariantsFragment(dark = IconVariantsFragment.Dark(svgUrl = ""), light = IconVariantsFragment.Light(svgUrl = ""))
                )
            ))),
            EmbarkStoryQuery.InsuranceProvider(name = "Länsförsäkringar", logo = EmbarkStoryQuery.Logo(variants = EmbarkStoryQuery.Variants(
                fragments = EmbarkStoryQuery.Variants.Fragments(
                    iconVariantsFragment = IconVariantsFragment(dark = IconVariantsFragment.Dark(svgUrl = ""), light = IconVariantsFragment.Light(svgUrl = ""))
                )
            )))
        )))
    )
}
