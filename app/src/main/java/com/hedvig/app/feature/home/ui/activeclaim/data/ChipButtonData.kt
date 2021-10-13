package com.hedvig.app.feature.home.ui.activeclaim.data

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R

data class ChipButtonData(val text: String, val type: ButtonType) {

    sealed class ButtonType {
        data class Contained(val color: Color) : ButtonType()
        object Outlined : ButtonType()
    }

    companion object {
        @Composable
        fun buttonListFromActiveClaim(activeClaim: HomeQuery.ActiveClaim): List<ChipButtonData> {
            return when (activeClaim.status) {
                ClaimStatus.SUBMITTED -> listOf(ChipButtonData("CLAIM", ButtonType.Outlined))
                ClaimStatus.BEING_HANDLED -> listOf(ChipButtonData("CLAIM", ButtonType.Outlined))
                ClaimStatus.CLOSED -> {
                    when (activeClaim.outcome) {
                        ClaimOutcome.PAID -> {
                            mutableListOf<ChipButtonData>().apply {
                                val payout = activeClaim.payout
                                add(
                                    ChipButtonData(
                                        "PAID",
                                        ButtonType.Contained(colorResource(R.color.colorInfoCardSurface))
                                    )
                                )
                                if (payout != null) {
                                    add(
                                        ChipButtonData(
                                            // TODO proper currency handling? Does GraphQL return this properly?
                                            "${payout.amount} ${payout.currency}",
                                            ButtonType.Contained(MaterialTheme.colors.primary)
                                        )
                                    )
                                }
                            }.toList()
                        }
                        ClaimOutcome.NOT_COMPENSATED -> listOf(
                            ChipButtonData(
                                "NOT COMPENSATED",
                                ButtonType.Contained(MaterialTheme.colors.primary)
                            )
                        )
                        ClaimOutcome.NOT_COVERED -> listOf(
                            ChipButtonData(
                                "NOT COVERED",
                                ButtonType.Contained(MaterialTheme.colors.primary)
                            )
                        )
                        else -> throw IllegalArgumentException()
                    }
                }
                ClaimStatus.REOPENED -> listOf(
                    // TODO proper dark theme color
                    ChipButtonData("REOPENED", ButtonType.Contained(Color(0xFFFCBA8D)))
                )
                ClaimStatus.UNKNOWN__ -> throw IllegalArgumentException()
            }
        }
    }
}
