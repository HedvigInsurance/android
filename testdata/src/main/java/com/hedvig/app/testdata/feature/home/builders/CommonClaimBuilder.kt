package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.HedvigColor

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
                fragments = HomeQuery.Variants.Fragments(
                    IconVariantsFragment(
                        dark = IconVariantsFragment.Dark(
                            svgUrl = "/app-content-service/warning_dark.svg"
                        ),
                        light = IconVariantsFragment.Light(
                            svgUrl = "/app-content-service/warning.svg"
                        )
                    )
                )
            )
        ),
        layout = HomeQuery.Layout(
            asTitleAndBulletPoints = if (variant == Variant.TITLE_AND_BULLET_POINTS) {
                HomeQuery.AsTitleAndBulletPoints(
                    bulletPoints = emptyList(),
                    buttonTitle = "",
                    color = HedvigColor.BLACK,
                    title = title
                )
            } else {
                null
            },
            asEmergency = if (variant == Variant.EMERGENCY) {
                HomeQuery.AsEmergency(
                    color = HedvigColor.BLACK,
                    emergencyNumber = emergencyNumber,
                )
            } else {
                null
            }
        )
    )

    enum class Variant {
        EMERGENCY,
        TITLE_AND_BULLET_POINTS
    }
}
