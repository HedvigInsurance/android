package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.datetime.LocalDate

internal class ManualChargeViewModel : MoleculeViewModel<ManualChargeEvent, ManualChargeUiState>(
  initialState = ManualChargeUiState.Loading,
  presenter = ManualChargePresenter(),
)

private class ManualChargePresenter : MoleculePresenter<ManualChargeEvent, ManualChargeUiState> {
  @Composable
  override fun MoleculePresenterScope<ManualChargeEvent>.present(
    lastState: ManualChargeUiState,
  ): ManualChargeUiState {
    // TODO: Implement presenter logic
    return ManualChargeUiState.Loading
  }
}

internal sealed interface ManualChargeUiState {
  data object Loading : ManualChargeUiState

  data class Failure(
    val reason: ManualChargeFailureReason
  ) : ManualChargeUiState

  data class Success(
    val dueDate: LocalDate,
    val amount: UiMoney
  ) : ManualChargeUiState
}

internal interface ManualChargeFailureReason {
  data object NotAllowed: ManualChargeFailureReason
  data object GeneralFailure: ManualChargeFailureReason
  data class UserErrorWithMessage(
    val message: String
  ): ManualChargeFailureReason
}

internal sealed interface ManualChargeEvent {
  data object Retry : ManualChargeEvent

  // TODO: Add events
}

