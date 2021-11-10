package com.hedvig.app.feature.home.ui.claimstatus.data

import android.content.res.Resources
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.R
import com.hedvig.app.util.apollo.format
import com.hedvig.app.util.apollo.toMonetaryAmount
import com.hedvig.app.util.compose.DarkAndLightColor
import java.util.Locale

data class PillData(
    val text: String,
    val type: PillType
) {

    sealed class PillType {
        data class Contained(val color: DarkAndLightColor) : PillType()
        object Outlined : PillType()
    }

    companion object {
        fun fromClaimStatus(
            homeQueryClaim: HomeQuery.Claim,
            resources: Resources,
            locale: Locale
        ): List<PillData> {
            return listOfPillData(homeQueryClaim, resources, locale)
                .map { pillData: PillData ->
                    pillData.copy(text = pillData.text.uppercase(locale))
                }
        }

        private fun listOfPillData(
            homeQueryClaim: HomeQuery.Claim,
            resources: Resources,
            locale: Locale
        ): List<PillData> = when (homeQueryClaim.status) {
            ClaimStatus.SUBMITTED -> listOf(
                PillData(
                    resources.getString(R.string.home_claim_card_pill_claim),
                    PillType.Outlined,
                ),
            )
            ClaimStatus.BEING_HANDLED -> listOf(
                PillData(
                    resources.getString(R.string.home_claim_card_pill_claim),
                    PillType.Outlined,
                ),
            )
            ClaimStatus.CLOSED -> {
                when (homeQueryClaim.outcome) {
                    ClaimOutcome.PAID -> {
                        mutableListOf<PillData>().apply {
                            add(
                                PillData(
                                    resources.getString(R.string.claim_decision_paid),
                                    PillType.Contained(DarkAndLightColor.primary()),
                                ),
                            )
                            homeQueryClaim.payout?.let { payout: HomeQuery.Payout ->
                                add(
                                    PillData(
                                        payout.fragments.monetaryAmountFragment
                                            .toMonetaryAmount()
                                            .format(locale, 0),
                                        PillType.Contained(ClaimStatusColors.Pill.paid)
                                    )
                                )
                            }
                        }.toList()
                    }
                    ClaimOutcome.NOT_COMPENSATED -> listOf(
                        PillData(
                            resources.getString(R.string.claim_decision_not_compensated),
                            PillType.Contained(DarkAndLightColor.primary())
                        )
                    )
                    ClaimOutcome.NOT_COVERED -> listOf(
                        PillData(
                            resources.getString(R.string.claim_decision_not_covered),
                            PillType.Contained(DarkAndLightColor.primary())
                        )
                    )
                    else -> listOf(
                        PillData(
                            resources.getString(R.string.home_claim_card_pill_claim),
                            PillType.Outlined,
                        )
                    )
                }
            }
            ClaimStatus.REOPENED -> listOf(
                PillData(
                    resources.getString(R.string.home_claim_card_pill_reopened),
                    PillType.Contained(ClaimStatusColors.Pill.reopened)
                ),
                PillData(
                    resources.getString(R.string.home_claim_card_pill_claim),
                    PillType.Outlined
                ),
            )
            else -> listOf(
                PillData(
                    resources.getString(R.string.home_claim_card_pill_claim),
                    PillType.Outlined,
                ),
            )
        }
    }
}
