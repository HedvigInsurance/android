package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimtriaging.ClaimGroupId
import com.hedvig.android.data.claimtriaging.EntryPoint
import com.hedvig.android.data.claimtriaging.GetEntryPointsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimEntryPointsViewModel(
  private val claimGroupId: ClaimGroupId,
  private val getClaimEntryPoints: GetEntryPointsUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimEntryPointsUiState())
  val uiState = _uiState.asStateFlow()

  init {
    loadEntryPoints()
  }

  fun loadEntryPoints() {
    _uiState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getClaimEntryPoints.invoke(claimGroupId).fold(
        ifLeft = { errorMessage ->
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
              entryPoints = entryPoints,
              selectedEntryPoint = null,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectEntryPoint(entryPoint: EntryPoint) {
    _uiState.update { it.copy(selectedEntryPoint = entryPoint) }
  }
}

internal data class ClaimEntryPointsUiState(
  val entryPoints: ImmutableList<EntryPoint> = persistentListOf(),
  val selectedEntryPoint: EntryPoint? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
) {
  val canContinue: Boolean
    get() = selectedEntryPoint != null
}
