package com.hedvig.android.feature.terminateinsurance.step.choose

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.terminateinsurance.data.GetContractsEligibleToTerminateUseCase
import com.hedvig.android.feature.terminateinsurance.data.InsuranceEligibleForTermination
import com.hedvig.android.feature.terminateinsurance.data.TerminateInsuranceStep
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseInsuranceToTerminateViewModel(
  getContractsEligibleToTerminateUseCase: GetContractsEligibleToTerminateUseCase,
) : MoleculeViewModel<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState>(
    initialState = ChooseInsuranceToTerminateStepUiState.Loading,
    presenter = ChooseInsuranceToTerminatePresenter(getContractsEligibleToTerminateUseCase),
  )

private class ChooseInsuranceToTerminatePresenter(
  private val getContractsEligibleToTerminateUseCase: GetContractsEligibleToTerminateUseCase,
) : MoleculePresenter<ChooseInsuranceToTerminateEvent, ChooseInsuranceToTerminateStepUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseInsuranceToTerminateEvent>.present(
    lastState: ChooseInsuranceToTerminateStepUiState,
  ): ChooseInsuranceToTerminateStepUiState {
    TODO("Not yet implemented")
  }
}

internal sealed interface ChooseInsuranceToTerminateEvent {
  data class SelectInsurance(val insuranceId: String) : ChooseInsuranceToTerminateEvent
}

internal sealed interface ChooseInsuranceToTerminateStepUiState {
  data object Loading : ChooseInsuranceToTerminateStepUiState

  data class Success(
    val insuranceList: List<InsuranceEligibleForTermination>,
    val nextStep: TerminateInsuranceStep,
    val continueEnabled: Boolean,
    val selectedId: String?,
  ) : ChooseInsuranceToTerminateStepUiState

  data object Failure : ChooseInsuranceToTerminateStepUiState
}
