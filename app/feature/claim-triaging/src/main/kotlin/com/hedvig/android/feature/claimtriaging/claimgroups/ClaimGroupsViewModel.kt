package com.hedvig.android.feature.claimtriaging.claimgroups

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.data.claimtriaging.ClaimGroup
import com.hedvig.android.feature.claimtriaging.GetEntryPointGroupsUseCase
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ClaimGroupsViewModel(
  private val getEntryPointGroupsUseCase: GetEntryPointGroupsUseCase,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ClaimGroupsUiState())
  val uiState = _uiState.asStateFlow()

  init {
    loadClaimGroups()
  }

  fun loadClaimGroups() {
    _uiState.update { it.copy(errorMessage = null, isLoading = true) }
    viewModelScope.launch {
      getEntryPointGroupsUseCase.invoke().fold(
        ifLeft = { errorMessage ->
          _uiState.update {
            it.copy(
              errorMessage = errorMessage.message,
              isLoading = false,
            )
          }
        },
        ifRight = { claimGroups ->
          _uiState.update {
            it.copy(
              claimGroups = claimGroups,
              selectedClaimGroup = null,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSelectClaimGroup(claimGroup: ClaimGroup) {
    _uiState.update { it.copy(selectedClaimGroup = claimGroup) }
  }
}

@Immutable
internal data class ClaimGroupsUiState(
  val claimGroups: ImmutableList<ClaimGroup> = persistentListOf(),
  val selectedClaimGroup: ClaimGroup? = null,
  val errorMessage: String? = null,
  val isLoading: Boolean = true,
) {
  val canContinue: Boolean
    get() = selectedClaimGroup != null
}
