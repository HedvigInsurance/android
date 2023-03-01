package com.hedvig.android.odyssey.input

import com.hedvig.android.odyssey.model.ClaimState
import com.hedvig.android.odyssey.model.Input
import com.hedvig.android.odyssey.model.Resolution

data class InputViewState(
  val title: String = "Submit Claim",
  val claimState: ClaimState = ClaimState(),
  val phoneNumber: String = "",
  val inputs: List<Input> = emptyList(),
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val shouldExit: Boolean = false,
  val resolution: Resolution = Resolution.None,
  val currentInputIndex: Int = 0,
) {
  val currentInput = inputs.getOrElse(currentInputIndex) { null }
}
