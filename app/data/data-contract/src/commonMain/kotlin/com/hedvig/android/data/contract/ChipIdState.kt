package com.hedvig.android.data.contract

sealed interface ChipIdState {
  data object Missing : ChipIdState

  data object NotRequired : ChipIdState
}
