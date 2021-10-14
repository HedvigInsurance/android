package com.hedvig.app.feature.home.ui.claimstatus.data

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.ui.compose.theme.ClaimStatusColors

data class PillData(val text: String, val type: PillType) {

    sealed class PillType {
        data class Contained(val color: Color) : PillType()
        object Outlined : PillType()
    }

    companion object {
        @Composable
        fun fromClaimStatus(claimStatus: HomeQuery.ClaimStatus): List<PillData> {
            return when (claimStatus.status) {
                ClaimStatus.SUBMITTED -> listOf(PillData("CLAIM", PillType.Outlined))
                ClaimStatus.BEING_HANDLED -> listOf(PillData("CLAIM", PillType.Outlined))
                ClaimStatus.CLOSED -> {
                    when (claimStatus.outcome) {
                        ClaimOutcome.PAID -> {
                            mutableListOf<PillData>().apply {
                                add(
                                    PillData(
                                        stringResource(R.string.claim_decision_paid),
                                        PillType.Contained(MaterialTheme.colors.primary)
                                    )
                                )
                                claimStatus.payout?.let { payout: HomeQuery.Payout ->
                                    add(
                                        PillData(
                                            "${payout.amount} ${payout.currency}",
                                            PillType.Contained(ClaimStatusColors.Pill.paid)
                                        )
                                    )
                                }
                            }.toList()
                        }
                        ClaimOutcome.NOT_COMPENSATED -> listOf(
                            PillData(
                                stringResource(R.string.claim_decision_not_compensated),
                                PillType.Contained(MaterialTheme.colors.primary)
                            )
                        )
                        ClaimOutcome.NOT_COVERED -> listOf(
                            PillData(
                                stringResource(R.string.claim_decision_not_covered),
                                PillType.Contained(MaterialTheme.colors.primary)
                            )
                        )
                        else -> throw IllegalArgumentException()
                    }
                }
                ClaimStatus.REOPENED -> listOf(
                    PillData(
                        stringResource(R.string.home_claim_card_pill_reopened),
                        PillType.Contained(ClaimStatusColors.Pill.reopened)
                    ),
                    PillData(
                        stringResource(R.string.home_claim_card_pill_claim),
                        PillType.Outlined
                    ),
                )
                ClaimStatus.UNKNOWN__ -> throw IllegalArgumentException()
            }
        }
    }
}
