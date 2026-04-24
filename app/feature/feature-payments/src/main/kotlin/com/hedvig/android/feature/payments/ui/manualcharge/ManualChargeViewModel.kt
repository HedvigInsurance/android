package com.hedvig.android.feature.payments.ui.manualcharge

import androidx.compose.runtime.Composable
import com.hedvig.android.core.uidata.UiMoney
import com.hedvig.android.feature.payments.data.GetManualChargeInfoUseCase
import com.hedvig.android.feature.payments.data.ManualChargeInfo
import com.hedvig.android.feature.payments.data.TriggerManualChargeUseCase
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import com.hedvig.android.molecule.public.MoleculeViewModel
import kotlinx.datetime.LocalDate

internal class ManualChargeViewModel(
  getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  triggerManualCharge: TriggerManualChargeUseCase
) : MoleculeViewModel<ManualChargeEvent, ManualChargeUiState>(
  initialState = ManualChargeUiState.Loading,
  presenter = ManualChargePresenter(getManualChargeInfoUseCase, triggerManualCharge),
)

private class ManualChargePresenter(
  private val getManualChargeInfoUseCase: GetManualChargeInfoUseCase,
  private val triggerManualCharge: TriggerManualChargeUseCase
) : MoleculePresenter<ManualChargeEvent, ManualChargeUiState> {
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
    val manualChargeInfo: ManualChargeInfo
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

