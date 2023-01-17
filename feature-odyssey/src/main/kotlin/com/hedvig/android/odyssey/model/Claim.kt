package com.hedvig.android.odyssey.model

import com.hedvig.android.odyssey.repository.AutomationClaimDTO2
import com.hedvig.android.odyssey.repository.AutomationClaimInputDTO2
import com.hedvig.common.remote.money.MonetaryAmount
import java.time.LocalDate

data class Claim(
  val state: ClaimState,
  val inputs: List<Input> = emptyList(),
  val resolutions: Set<Resolution> = emptySet(),
) {
  data class ClaimState(
    val dateOfOccurrence: LocalDate? = LocalDate.now(),
    val audioUrl: String? = null,
    val location: AutomationClaimDTO2.ClaimLocation = AutomationClaimDTO2.ClaimLocation.AT_HOME,
    val item: ItemState = ItemState(),
  ) {
    data class ItemState(
      val purchasePrice: MonetaryAmount? = null,
      val purchaseDate: LocalDate? = LocalDate.now(),
      val problemIds: List<AutomationClaimInputDTO2.SingleItem.ClaimProblem> = listOf(AutomationClaimInputDTO2.SingleItem.ClaimProblem.BROKEN_FRONT),
    )
  }
}
