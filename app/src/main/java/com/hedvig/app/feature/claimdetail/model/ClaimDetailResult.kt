package com.hedvig.app.feature.claimdetail.model

import com.hedvig.android.owldroid.graphql.ClaimDetailsQuery
import com.hedvig.android.owldroid.type.ClaimOutcome
import com.hedvig.android.owldroid.type.ClaimStatus
import javax.money.MonetaryAmount

sealed class ClaimDetailResult {
    object Open : ClaimDetailResult()
    sealed class Closed : ClaimDetailResult() {
        data class Paid(val monetaryAmount: MonetaryAmount?) : Closed()
        object NotCompensated : Closed()
        object NotCovered : Closed()
    }

    companion object {
        fun fromDto(dto: ClaimDetailsQuery.ClaimDetail): ClaimDetailResult {
            val claim = dto.claim
            return when (claim.status) {
                ClaimStatus.CLOSED -> {
                    when (claim.outcome) {
                        ClaimOutcome.PAID -> {
                            // todo Uncomment when the backend returns the proper payment amount
                            // https://hedviginsurance.slack.com/archives/CN55X38T1/p1640192642025300?thread_ts=1639987758.015100&cid=CN55X38T1
//                            val monetaryAmount = claim
//                                .payout
//                                ?.fragments
//                                ?.monetaryAmountFragment
//                                ?.toMonetaryAmount()
                            val monetaryAmount: MonetaryAmount? = null
                            Closed.Paid(monetaryAmount)
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
