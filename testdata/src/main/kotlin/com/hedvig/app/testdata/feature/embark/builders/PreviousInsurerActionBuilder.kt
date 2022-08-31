package com.hedvig.app.testdata.feature.embark.builders

import com.hedvig.android.owldroid.graphql.EmbarkStoryQuery
import com.hedvig.android.owldroid.graphql.fragment.EmbarkLinkFragment
import com.hedvig.android.owldroid.graphql.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.type.EmbarkPreviousInsuranceProviderAction

data class PreviousInsurerActionBuilder(
  val storeKey: String = "BAR",
  val next: EmbarkLinkFragment,
) {
  fun build() = EmbarkStoryQuery.Action(
    __typename = EmbarkPreviousInsuranceProviderAction.type.name,
    asEmbarkSelectAction = null,
    asEmbarkTextAction = null,
    asEmbarkTextActionSet = null,
    asEmbarkPreviousInsuranceProviderAction = EmbarkStoryQuery.AsEmbarkPreviousInsuranceProviderAction(
      __typename = EmbarkPreviousInsuranceProviderAction.type.name,
      previousInsurerData = EmbarkStoryQuery.PreviousInsurerData(
        insuranceProviders = listOf(
          EmbarkStoryQuery.InsuranceProvider(
            id = "if",
            name = "IF",
            logo = EmbarkStoryQuery.Logo(
              variants = EmbarkStoryQuery.Variants(
                __typename = "",
                fragments = EmbarkStoryQuery.Variants.Fragments(
                  iconVariantsFragment = IconVariantsFragment(
                    dark = IconVariantsFragment.Dark(svgUrl = ""),
                    light = IconVariantsFragment.Light(svgUrl = "/app-content-service/if.svg"),
                  ),
                ),
              ),
            ),
          ),
          EmbarkStoryQuery.InsuranceProvider(
            id = "trygghansa",
            name = "Trygg-Hansa",
            logo = EmbarkStoryQuery.Logo(
              variants = EmbarkStoryQuery.Variants(
                __typename = "",
                fragments = EmbarkStoryQuery.Variants.Fragments(
                  iconVariantsFragment = IconVariantsFragment(
                    dark = IconVariantsFragment.Dark(svgUrl = ""),
                    light = IconVariantsFragment.Light(
                      svgUrl = "/app-content-service/trygg_hansa.svg",
                    ),
                  ),
                ),
              ),
            ),
          ),
          EmbarkStoryQuery.InsuranceProvider(
            id = "folksam",
            name = "Folksam",
            logo = EmbarkStoryQuery.Logo(
              variants = EmbarkStoryQuery.Variants(
                __typename = "",
                fragments = EmbarkStoryQuery.Variants.Fragments(
                  iconVariantsFragment = IconVariantsFragment(
                    dark = IconVariantsFragment.Dark(svgUrl = ""),
                    light = IconVariantsFragment.Light(svgUrl = "/app-content-service/folksam.svg"),
                  ),
                ),
              ),
            ),
          ),
        ),
        storeKey = storeKey,
        next = EmbarkStoryQuery.Next(
          __typename = "",
          fragments = EmbarkStoryQuery.Next.Fragments(next),
        ),
        skip = EmbarkStoryQuery.Skip(
          __typename = "",
          fragments = EmbarkStoryQuery.Skip.Fragments(
            EmbarkLinkFragment(
              name = "test_skip",
              label = "Skip",
              hidden = false,
            ),
          ),
        ),
      ),
    ),
    asEmbarkNumberAction = null,
    asEmbarkNumberActionSet = null,
    asEmbarkDatePickerAction = null,
    asEmbarkMultiAction = null,
    asEmbarkAudioRecorderAction = null,
    asEmbarkExternalInsuranceProviderAction = null,
    asEmbarkAddressAutocompleteAction = null,
  )
}
