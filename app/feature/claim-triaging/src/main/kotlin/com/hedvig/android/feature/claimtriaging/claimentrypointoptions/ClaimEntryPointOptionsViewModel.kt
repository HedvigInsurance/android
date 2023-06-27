package com.hedvig.android.feature.claimtriaging.claimentrypointoptions

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import com.hedvig.android.data.claimtriaging.EntryPointOption
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

internal class ClaimEntryPointOptionsViewModel(
  entryPointOptions: ImmutableList<EntryPointOption>,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimEntryPointOptionsUiState(entryPointOptions))
  val uiState = _uiState.asStateFlow()

  fun onSelectEntryPoint(entryPointOption: EntryPointOption) {
    _uiState.update { it.copy(selectedEntryPointOption = entryPointOption) }
  }
}

@Immutable
internal data class ClaimEntryPointOptionsUiState(
  val entryPointOptions: ImmutableList<EntryPointOption>,
  val selectedEntryPointOption: EntryPointOption? = null,
) {
  val canContinue: Boolean
    get() = selectedEntryPointOption != null
}
