package com.hedvig.android.ui.claimstatus.model

import com.hedvig.android.core.uidata.UiMoney
import octopus.fragment.ClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus

sealed interface ClaimPillType {
  data object Open : ClaimPillType

  data object Reopened : ClaimPillType

  data class PaymentAmount(val uiMoney: UiMoney) : ClaimPillType

  data object Unknown : ClaimPillType

  sealed interface Closed : ClaimPillType {
    data object NotCompensated : Closed

    data object NotCovered : Closed

    data object Paid : Closed
  }

  companion object {
    fun fromClaimFragment(claim: ClaimFragment): List<ClaimPillType> = when (claim.status) {
      ClaimStatus.CREATED -> listOf(Open)
      ClaimStatus.IN_PROGRESS -> listOf(Open)
      ClaimStatus.CLOSED -> {
        when (claim.outcome) {
          ClaimOutcome.PAID -> {
            buildList {
              add(Closed.Paid)
              val payoutAmount = claim.payoutAmount
              if (payoutAmount != null) {
                add(PaymentAmount(UiMoney.fromMoneyFragment(payoutAmount)))
              }
            }
          }
          ClaimOutcome.NOT_COMPENSATED -> listOf(Closed.NotCompensated)
          ClaimOutcome.NOT_COVERED -> listOf(Closed.NotCovered)
          ClaimOutcome.UNKNOWN__,
          null,
          -> emptyList()
        }
      }
      ClaimStatus.REOPENED -> listOf(Reopened, Open)
      ClaimStatus.UNKNOWN__,
      null,
      -> emptyList()
    }
  }
}
