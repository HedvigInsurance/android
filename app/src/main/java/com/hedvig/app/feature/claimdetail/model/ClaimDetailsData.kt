package com.hedvig.app.feature.claimdetail.model

import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import com.hedvig.app.feature.home.ui.claimstatus.data.ClaimProgressData
import com.hedvig.app.util.apollo.toMonetaryAmount
import java.time.Instant
import javax.money.MonetaryAmount

data class ClaimDetailsData(
    val claimType: String,
    val insuranceType: String,
    val claimResult: ClaimResult = ClaimResult.Open,
    val submittedAt: Instant,
    val closedAt: Instant?,
    val cardData: CardData,
    val audioStuff: Any? = null, // todo make a type for this
) {
    sealed class ClaimResult {
        object Open : ClaimResult()
        sealed class Closed : ClaimResult() {
            data class Paid(val monetaryAmount: MonetaryAmount) : Closed()
            object NotCompensated : Closed()
            object NotCovered : Closed()
        }

        companion object {
            fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): ClaimResult {
                val claim = dto.claim
                return when (claim.status) {
                    ClaimStatus.CLOSED -> {
                        when (claim.outcome) {
                            ClaimOutcome.PAID -> {
                                val monetaryAmount = claim
                                    .payout
                                    ?.fragments
                                    ?.monetaryAmountFragment
                                    ?.toMonetaryAmount()
                                if (monetaryAmount != null) {
                                    // todo Uncomment when the backend returns the proper payment amount
                                    // https://hedviginsurance.slack.com/archives/CN55X38T1/p1640192642025300?thread_ts=1639987758.015100&cid=CN55X38T1
                                    // Closed.Paid(monetaryAmount)
                                    Open
                                } else {
                                    Open
                                }
                            }
                            ClaimOutcome.NOT_COMPENSATED -> Closed.NotCompensated
                            ClaimOutcome.NOT_COVERED -> Closed.NotCovered
                            else -> Open
                        }
                    }
                    else -> Open
                }
            }
        }
    }

    data class CardData(
        val progress: List<ClaimProgressData>,
        val statusParagraph: String,
    ) {
        companion object {
            fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): CardData {
                return CardData(
                    progress = ClaimProgressData.fromProgressSegments(
                        dto.progressSegments.map { it.fragments.progressSegments }
                    ),
                    statusParagraph = dto.claim.statusParagraph
                )
            }
        }
    }

    companion object {
        fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): ClaimDetailsData {
            return ClaimDetailsData(
                claimType = dto.title,
                insuranceType = dto.subtitle,
                claimResult = ClaimResult.fromDto(dto),
                submittedAt = dto.claim.submittedAt,
                closedAt = dto.claim.closedAt,
                cardData = CardData.fromDto(dto),
                audioStuff = dto.claim.signedAudioURL,
            )
        }
    }
}
