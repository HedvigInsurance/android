package com.hedvig.android.ui.claimstatus.model

import kotlin.time.Instant

sealed interface ClaimCardUiState {
  data class Claim(val uiState: ClaimStatusCardUiState) : ClaimCardUiState

  data class Draft(
    val id: String,
    val title: String?,
    val startedAt: Instant,
  ) : ClaimCardUiState
}
