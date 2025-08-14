package com.hedvig.android.feature.claimhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import arrow.core.NonEmptyList
import arrow.core.merge
import arrow.core.raise.either
import arrow.core.raise.ensureNotNull
import arrow.core.toNonEmptyListOrNull
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest

internal class ClaimHistoryViewModel(
  getClaimsHistoryUseCase: GetClaimsHistoryUseCase,
) : MoleculeViewModel<ClaimHistoryEvent, ClaimHistoryUiState>(
    ClaimHistoryUiState.Loading,
    ClaimHistoryPresenter(getClaimsHistoryUseCase),
  )

private class ClaimHistoryPresenter(
  private val getClaimsHistoryUseCase: GetClaimsHistoryUseCase,
) : MoleculePresenter<ClaimHistoryEvent, ClaimHistoryUiState> {
  @Composable
  override fun MoleculePresenterScope<ClaimHistoryEvent>.present(lastState: ClaimHistoryUiState): ClaimHistoryUiState {
    var state by remember { mutableStateOf(lastState) }
    var loadIteration by remember { mutableIntStateOf(0) }
    CollectEvents { event ->
      when (event) {
        ClaimHistoryEvent.Reload -> loadIteration++
      }
    }
    LaunchedEffect(loadIteration) {
      getClaimsHistoryUseCase.invoke().collectLatest { result ->
        state = either {
          val list = result
            .mapLeft { ClaimHistoryUiState.Error }
            .bind()
            .toNonEmptyListOrNull()
          ensureNotNull(list) {
            ClaimHistoryUiState.NoHistory
          }
          ClaimHistoryUiState.Content(list)
        }.merge()
      }
    }
    return state
  }
}

internal sealed interface ClaimHistoryEvent {
  data object Reload : ClaimHistoryEvent
}

internal sealed interface ClaimHistoryUiState {
  data object Loading : ClaimHistoryUiState

  data object NoHistory : ClaimHistoryUiState

  data object Error : ClaimHistoryUiState

  data class Content(val claims: NonEmptyList<ClaimHistory>) : ClaimHistoryUiState
}
