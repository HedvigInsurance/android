package com.hedvig.android.ui.claimstatus.model

import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.logger.logcat
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
    fun fromClaimFragment(claim: ClaimFragment): List<ClaimPillType> {
      logcat { "Mariia:claim type: ${claim.claimType} claim.status ${claim.status}  claim.outcome: ${claim.outcome}" }
      return when (claim.status) {
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
            ClaimOutcome.UNRESPONSIVE -> listOf(Closed.GenericClosed, Closed.Unresponsive)
            ClaimOutcome.UNKNOWN__,
            null,
            -> emptyList()
          }
        }
        ClaimStatus.REOPENED -> listOf(Claim)
        ClaimStatus.UNKNOWN__,
        null,
        -> emptyList()
      }
    }
  }
}
