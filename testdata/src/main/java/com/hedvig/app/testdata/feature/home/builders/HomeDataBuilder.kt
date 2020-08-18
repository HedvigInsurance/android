package com.hedvig.app.testdata.feature.home.builders

import com.hedvig.android.owldroid.fragment.IconVariantsFragment
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.HedvigColor
import java.time.LocalDate

data class HomeDataBuilder(
    private val contracts: List<Status> = emptyList(),
    private val firstName: String? = "Test",
    private val commonClaims: List<HomeQuery.CommonClaim> = listOf(
        CommonClaimBuilder(title = "Trasig telefon").build(),
        CommonClaimBuilder(title = "Försenat bagage").build()
    )
) {
    fun build() = HomeQuery.Data(
        member = HomeQuery.Member(
            firstName = firstName
        ),
        contracts = contracts.map { c ->
            HomeQuery.Contract(
                switchedFromInsuranceProvider = null,
                status = HomeQuery.Status(
                    asPendingStatus = if (c == Status.PENDING) {
                        HomeQuery.AsPendingStatus(
                            pendingSince = null
                        )
                    } else {
                        null
                    },
                    asActiveInFutureStatus = when (c) {
                        Status.ACTIVE_IN_FUTURE -> HomeQuery.AsActiveInFutureStatus(
                            futureInception = LocalDate.of(2025, 1, 1)
                        )
                        Status.ACTIVE_IN_FUTURE_INVALID -> HomeQuery.AsActiveInFutureStatus(
                            futureInception = null
                        )
                        else -> null
                    },
                    asActiveInFutureAndTerminatedInFutureStatus = if (c == Status.ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE) {
                        HomeQuery.AsActiveInFutureAndTerminatedInFutureStatus(
                            futureInception = LocalDate.of(2024, 1, 1)
                        )
                    } else {
                        null
                    },
                    asActiveStatus = if (c == Status.ACTIVE) {
                        HomeQuery.AsActiveStatus(
                            pastInception = LocalDate.now()
                        )
                    } else {
                        null
                    },
                    asTerminatedStatus = if (c == Status.TERMINATED) {
                        HomeQuery.AsTerminatedStatus(
                            termination = null
                        )
                    } else {
                        null
                    }
                )
            )
        },
        isEligibleToCreateClaim = contracts.any { it == Status.ACTIVE },
        commonClaims = commonClaims
    )

    enum class Status {
        PENDING,
        ACTIVE_IN_FUTURE,
        ACTIVE_IN_FUTURE_AND_TERMINATED_IN_FUTURE,
        ACTIVE_IN_FUTURE_INVALID,
        ACTIVE,
        TERMINATED
    }
}

data class CommonClaimBuilder(
    val title: String = "Example",
    val variant: Variant = Variant.TITLE_AND_BULLET_POINTS
) {
    fun build() = HomeQuery.CommonClaim(
        title = title,
        icon = HomeQuery.Icon(
            variants = HomeQuery.Variants(
                fragments = HomeQuery.Variants.Fragments(
                    IconVariantsFragment(
                        dark = IconVariantsFragment.Dark(
                            svgUrl = "https://www.example.com"
                        ),
                        light = IconVariantsFragment.Light(
                            svgUrl = "https://www.example.com"
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
                    title = ""
                )
            } else {
                null
            },
            asEmergency = if (variant == Variant.EMERGENCY) {
                HomeQuery.AsEmergency(
                    color = HedvigColor.BLACK
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
