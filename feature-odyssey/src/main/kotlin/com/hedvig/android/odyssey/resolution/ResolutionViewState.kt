package com.hedvig.android.odyssey.resolution

data class ResolutionViewState(
  val title: String = "Submit Claim",
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val isCompleted: Boolean = false,
)
