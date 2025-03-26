package com.hedvig.android.ui.claimstatus.model

import com.hedvig.android.core.uidata.UiMoney
import octopus.fragment.ClaimFragment
import octopus.type.ClaimOutcome
import octopus.type.ClaimStatus

sealed interface ClaimPillType {
  data object Claim : ClaimPillType

  data class PaymentAmount(val uiMoney: UiMoney) : ClaimPillType

  data object Unknown : ClaimPillType

  sealed interface Closed : ClaimPillType {
    data object GenericClosed : Closed

    data object NotCompensated : Closed

    data object NotCovered : Closed

    data object Paid : Closed

    data object Unresponsive : Closed
  }

  companion object {
    fun fromClaimFragment(claim: ClaimFragment): List<ClaimPillType> = when (claim.status) {
      ClaimStatus.CREATED -> listOf(Claim)
      ClaimStatus.IN_PROGRESS -> listOf(Claim)
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
          ClaimOutcome.NOT_COMPENSATED -> listOf(Closed.GenericClosed, Closed.NotCompensated)
          ClaimOutcome.NOT_COVERED -> listOf(Closed.GenericClosed, Closed.NotCovered)
          ClaimOutcome.UNKNOWN__,
          null,
          -> emptyList()

          ClaimOutcome.UNRESPONSIVE -> listOf(Closed.GenericClosed, Closed.Unresponsive)
        }
      }
      ClaimStatus.REOPENED -> listOf(Claim)
      ClaimStatus.UNKNOWN__,
      null,
      -> emptyList()
    }
  }
}
