package com.hedvig.android.feature.deleteaccount

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import com.hedvig.android.core.common.safeCast
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountState
import com.hedvig.android.feature.deleteaccount.data.DeleteAccountStateUseCase
import com.hedvig.android.feature.deleteaccount.data.RequestAccountDeletionUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import kotlinx.coroutines.flow.collectLatest

internal class DeleteAccountPresenter(
  private val requestAccountDeletionUseCase: RequestAccountDeletionUseCase,
  private val deleteAccountStateUseCase: DeleteAccountStateUseCase,
) : MoleculePresenter<DeleteAccountEvent, DeleteAccountUiState> {
  @Composable
  override fun MoleculePresenterScope<DeleteAccountEvent>.present(
    lastState: DeleteAccountUiState,
  ): DeleteAccountUiState {
    var loadIteration by remember { mutableIntStateOf(0) }
    var isPerformingDeletion by remember { mutableStateOf(false) }
    var failedToPerformAccountDeletion by remember {
      mutableStateOf(lastState.safeCast<DeleteAccountUiState.Success.CanDelete>()?.failedToPerformDeletion ?: false)
    }
    var deleteAccountState: DeleteAccountState? by remember {
      mutableStateOf(lastState.toDeleteAccountStateResult())
    }

    LaunchedEffect(loadIteration) {
      failedToPerformAccountDeletion = false
      if (deleteAccountState == DeleteAccountState.NetworkError) {
        deleteAccountState = null
      }
      deleteAccountStateUseCase.invoke().collectLatest { stateResult ->
        deleteAccountState = stateResult
      }
    }

    CollectEvents { event ->
      when (event) {
        DeleteAccountEvent.InitiateAccountDeletion -> isPerformingDeletion = true
        DeleteAccountEvent.RetryLoading -> loadIteration++
      }
    }

    LaunchedEffect(isPerformingDeletion) {
      if (!isPerformingDeletion) return@LaunchedEffect
      if (deleteAccountState !is DeleteAccountState.CanDelete) {
        isPerformingDeletion = false
        return@LaunchedEffect
      }
      val mutationResult = requestAccountDeletionUseCase.invoke()
      Snapshot.withMutableSnapshot {
        if (mutationResult.isLeft()) {
          failedToPerformAccountDeletion = true
        }
        isPerformingDeletion = false
      }
    }

    return when (deleteAccountState) {
      null -> DeleteAccountUiState.Loading
      DeleteAccountState.NetworkError -> DeleteAccountUiState.FailedToLoadDeleteAccountState
      DeleteAccountState.AlreadyRequestedDeletion -> DeleteAccountUiState.Success.AlreadyRequestedDeletion
      DeleteAccountState.HasActiveInsurance -> DeleteAccountUiState.Success.HasActiveInsurance
      DeleteAccountState.HasOngoingClaim -> DeleteAccountUiState.Success.HasOngoingClaim
      DeleteAccountState.CanDelete -> DeleteAccountUiState.Success.CanDelete(
        isPerformingDeletion = isPerformingDeletion,
        failedToPerformDeletion = failedToPerformAccountDeletion,
      )
    }
  }
}

internal sealed interface DeleteAccountEvent {
  object RetryLoading : DeleteAccountEvent

  object InitiateAccountDeletion : DeleteAccountEvent
}

internal sealed interface DeleteAccountUiState {
  data object Loading : DeleteAccountUiState

  data object FailedToLoadDeleteAccountState : DeleteAccountUiState

  sealed interface Success : DeleteAccountUiState {
    data object AlreadyRequestedDeletion : Success

    data object HasOngoingClaim : Success

    data object HasActiveInsurance : Success

    data class CanDelete(val isPerformingDeletion: Boolean, val failedToPerformDeletion: Boolean) : Success
  }
}

private fun DeleteAccountUiState.toDeleteAccountStateResult(): DeleteAccountState? {
  return when (this) {
    DeleteAccountUiState.FailedToLoadDeleteAccountState -> DeleteAccountState.NetworkError
    DeleteAccountUiState.Loading -> null
    DeleteAccountUiState.Success.AlreadyRequestedDeletion -> DeleteAccountState.AlreadyRequestedDeletion
    is DeleteAccountUiState.Success.CanDelete -> DeleteAccountState.CanDelete
    DeleteAccountUiState.Success.HasActiveInsurance -> DeleteAccountState.HasActiveInsurance
    DeleteAccountUiState.Success.HasOngoingClaim -> DeleteAccountState.HasOngoingClaim
  }
}
