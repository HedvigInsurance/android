package com.hedvig.android.odyssey

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution

data class ViewState(
  val title: String = "Submit Claim",
  val claimState: ClaimState = ClaimState(),
  val inputs: List<Input> = emptyList(),
  val resolutions: Set<Resolution> = emptySet(),
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val isLoadingPayment: Boolean = false,
  val isLastScreen: Boolean = false,
  val shouldExit: Boolean = false,
  val resolution: Resolution = Resolution.None,
  val currentInputIndex: Int = 0,
) {
  val currentInput = inputs.getOrElse(currentInputIndex) { null }
}
