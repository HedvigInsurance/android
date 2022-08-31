package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.apollo.graphql.HomeQuery
import com.hedvig.android.apollo.graphql.fragment.IconVariantsFragment
import com.hedvig.android.apollo.graphql.type.Emergency
import com.hedvig.android.apollo.graphql.type.HedvigColor
import com.hedvig.android.apollo.graphql.type.TitleAndBulletPoints

data class CommonClaimBuilder(
  val title: String = "Example",
  val variant: Variant = Variant.TITLE_AND_BULLET_POINTS,
  val emergencyNumber: String = "+46000000000",
) {
  fun build() = HomeQuery.CommonClaim(
    id = "123",
    title = title,
    icon = HomeQuery.Icon(
      variants = HomeQuery.Variants(
        __typename = "",
        fragments = HomeQuery.Variants.Fragments(
          IconVariantsFragment(
            dark = IconVariantsFragment.Dark(
              svgUrl = "/app-content-service/warning_dark.svg",
            ),
            light = IconVariantsFragment.Light(
              svgUrl = "/app-content-service/warning.svg",
            ),
          ),
        ),
      ),
    ),
    layout = HomeQuery.Layout(
      __typename = variant.typename,
      asTitleAndBulletPoints = if (variant == Variant.TITLE_AND_BULLET_POINTS) {
        HomeQuery.AsTitleAndBulletPoints(
          __typename = variant.typename,
          bulletPoints = emptyList(),
          buttonTitle = "",
          color = HedvigColor.Black,
          title = title,
        )
      } else {
        null
      },
      asEmergency = if (variant == Variant.EMERGENCY) {
        HomeQuery.AsEmergency(
          __typename = Emergency.type.name,
          color = HedvigColor.Black,
          emergencyNumber = emergencyNumber,
        )
      } else {
        null
      },
    ),
  )

  enum class Variant {
    EMERGENCY,
    TITLE_AND_BULLET_POINTS,
    ;

    val typename: String
      get() = when (this) {
        EMERGENCY -> Emergency.type.name
        TITLE_AND_BULLET_POINTS -> TitleAndBulletPoints.type.name
      }
  }
}
