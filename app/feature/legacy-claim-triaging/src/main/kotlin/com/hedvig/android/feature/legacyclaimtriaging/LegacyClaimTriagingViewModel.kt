package com.hedvig.android.feature.legacyclaimtriaging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.GetEntryPointsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import slimber.log.e

internal class LegacyClaimTriagingViewModel(
  private val getClaimEntryPoints: GetEntryPointsUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow(LegacyClaimTriagingUiState())
  val uiState: StateFlow<LegacyClaimTriagingUiState> = _uiState.asStateFlow()

  init {
    loadSearchableClaims()
  }

  fun loadSearchableClaims() {
    _uiState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke(claimGroupId = null).fold(
        ifLeft = { errorMessage ->
          e { "legacy claim triaging, getClaimEntryPoints failed: $errorMessage" }
          _uiState.update {
            it.copy(
              errorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { entryPoints: ImmutableList<EntryPoint> ->
          _uiState.update {
            it.copy(
              commonClaims = entryPoints,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaim(entryPoint: EntryPoint) {
    _uiState.update { it.copy(selectedClaim = entryPoint) }
  }
}

internal data class LegacyClaimTriagingUiState(
  val commonClaims: List<EntryPoint> = listOf(),
  val results: List<EntryPoint> = emptyList(),
  val selectedClaim: EntryPoint? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
)
