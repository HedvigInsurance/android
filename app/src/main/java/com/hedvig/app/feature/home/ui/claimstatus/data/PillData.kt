package com.hedvig.app.feature.home.ui.claimstatus.data

import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.ClaimStatusColors
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.compose.DarkAndLightColor
import com.hedvig.app.util.compose.DisplayableText

data class PillData(
    val displayableText: DisplayableText,
    val type: PillType
) {

    sealed class PillType {
        data class Contained(val color: DarkAndLightColor) : PillType()
        object Outlined : PillType()
    }

    companion object {
        fun fromClaimStatus(homeQueryClaim: HomeQuery.Claim): List<PillData> {
            return when (homeQueryClaim.status) {
                ClaimStatus.SUBMITTED -> listOf(
                    PillData(
                        DisplayableText(R.string.home_claim_card_pill_claim),
                        PillType.Outlined,
                    ),
                )
                ClaimStatus.BEING_HANDLED -> listOf(
                    PillData(
                        DisplayableText(R.string.home_claim_card_pill_claim),
                        PillType.Outlined,
                    ),
                )
                ClaimStatus.CLOSED -> {
                    when (homeQueryClaim.outcome) {
                        ClaimOutcome.PAID -> {
                            mutableListOf<PillData>().apply {
                                add(
                                    PillData(
                                        DisplayableText(R.string.claim_decision_paid),
                                        PillType.Contained(DarkAndLightColor.primary()),
                                    ),
                                )
                                homeQueryClaim.payout?.let { payout: HomeQuery.Payout ->
                                    add(
                                        PillData(
                                            DisplayableText(payout.fragments.monetaryAmountFragment.toMonetaryAmount()),
                                            PillType.Contained(ClaimStatusColors.Pill.paid)
                                        )
                                    )
                                }
                            }.toList()
                        }
                        ClaimOutcome.NOT_COMPENSATED -> listOf(
                            PillData(
                                DisplayableText(R.string.claim_decision_not_compensated),
                                PillType.Contained(DarkAndLightColor.primary())
                            )
                        )
                        ClaimOutcome.NOT_COVERED -> listOf(
                            PillData(
                                DisplayableText(R.string.claim_decision_not_covered),
                                PillType.Contained(DarkAndLightColor.primary())
                            )
                        )
                        else -> listOf(
                            PillData(
                                DisplayableText(R.string.home_claim_card_pill_claim),
                                PillType.Outlined,
                            )
                        )
                    }
                }
                ClaimStatus.REOPENED -> listOf(
                    PillData(
                        DisplayableText(R.string.home_claim_card_pill_reopened),
                        PillType.Contained(ClaimStatusColors.Pill.reopened)
                    ),
                    PillData(
                        DisplayableText(R.string.home_claim_card_pill_claim),
                        PillType.Outlined
                    ),
                )
                else -> listOf(
                    PillData(
                        DisplayableText(R.string.home_claim_card_pill_claim),
                        PillType.Outlined,
                    ),
                )
            }
        }
    }
}
