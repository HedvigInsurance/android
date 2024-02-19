package com.hedvig.android.feature.deleteaccount

import androidx.annotation.StringRes
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
      mutableStateOf(lastState.safeCast<DeleteAccountUiState.CanDelete>()?.failedToPerformDeletion ?: false)
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
      DeleteAccountState.AlreadyRequestedDeletion -> DeleteAccountUiState.CanNotDelete.AlreadyRequestedDeletion
      DeleteAccountState.HasActiveInsurance -> DeleteAccountUiState.CanNotDelete.HasActiveInsurance
      DeleteAccountState.HasOngoingClaim -> DeleteAccountUiState.CanNotDelete.HasOngoingClaim
      DeleteAccountState.CanDelete -> DeleteAccountUiState.CanDelete(
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

  sealed interface CanNotDelete : DeleteAccountUiState {
    data object AlreadyRequestedDeletion : CanNotDelete
    data object HasActiveInsurance : CanNotDelete
    data object HasOngoingClaim : CanNotDelete
  }

  data class CanDelete(val isPerformingDeletion: Boolean, val failedToPerformDeletion: Boolean) : DeleteAccountUiState
}

private fun DeleteAccountUiState.toDeleteAccountStateResult(): DeleteAccountState? {
  return when (this) {
    is DeleteAccountUiState.CanDelete -> DeleteAccountState.CanDelete
    DeleteAccountUiState.CanNotDelete.AlreadyRequestedDeletion -> DeleteAccountState.AlreadyRequestedDeletion
    DeleteAccountUiState.CanNotDelete.HasActiveInsurance -> DeleteAccountState.HasActiveInsurance
    DeleteAccountUiState.CanNotDelete.HasOngoingClaim -> DeleteAccountState.HasOngoingClaim
    DeleteAccountUiState.FailedToLoadDeleteAccountState -> DeleteAccountState.NetworkError
    DeleteAccountUiState.Loading -> null
  }
}
