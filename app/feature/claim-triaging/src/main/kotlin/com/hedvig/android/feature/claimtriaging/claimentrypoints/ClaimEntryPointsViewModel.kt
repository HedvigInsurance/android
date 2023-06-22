package com.hedvig.android.feature.claimtriaging.claimentrypoints

import androidx.lifecycle.ViewModel
import com.hedvig.android.data.claimtriaging.EntryPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class ClaimEntryPointsViewModel(
  private val entryPoints: ImmutableList<EntryPoint>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimEntryPointsUiState(entryPoints))
  val uiState = _uiState.asStateFlow()

  fun onSelectEntryPoint(entryPoint: EntryPoint) {
    _uiState.update { it.copy(selectedEntryPoint = entryPoint) }
  }
}

internal data class ClaimEntryPointsUiState(
  val entryPoints: ImmutableList<EntryPoint>,
  val selectedEntryPoint: EntryPoint? = null,
) {
  val canContinue: Boolean
    get() = selectedEntryPoint != null
}
