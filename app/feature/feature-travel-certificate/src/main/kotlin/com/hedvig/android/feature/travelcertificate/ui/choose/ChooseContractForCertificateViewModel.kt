package com.hedvig.android.feature.travelcertificate.ui.choose

import androidx.compose.runtime.Composable
import com.hedvig.android.data.travelcertificate.ContractEligibleWithAddress
import com.hedvig.android.data.travelcertificate.GetEligibleContractsWithAddressUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class ChooseContractForCertificateViewModel(
  getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
) : MoleculeViewModel<ChooseContractEvent, ChooseContractUiState>(
    initialState = ChooseContractUiState.Loading,
    presenter = ChooseContractPresenter(getEligibleContractsWithAddressUseCase),
  ) {
}

internal class ChooseContractPresenter(
  private val getEligibleContractsWithAddressUseCase: GetEligibleContractsWithAddressUseCase,
) : MoleculePresenter<ChooseContractEvent, ChooseContractUiState> {
  @Composable
  override fun MoleculePresenterScope<ChooseContractEvent>.present(
    lastState: ChooseContractUiState,
  ): ChooseContractUiState {
    // todo: replace mock
    return ChooseContractUiState.Success(
      listOf(
        ContractEligibleWithAddress("Morbydalen 12", "keuwhwkjfhjkeharfj"),
        ContractEligibleWithAddress("Akerbyvagen 257", "sesjhfhakerfhlwkeija"),
      ),
    )
  }
}

sealed interface ChooseContractEvent {
  data class ChangeContract(val contractId: String) : ChooseContractEvent

  data object RetryLoadData : ChooseContractEvent
}

internal sealed interface ChooseContractUiState {
  data object Loading : ChooseContractUiState

  data object Failure : ChooseContractUiState

  data class Success(val eligibleContracts: List<ContractEligibleWithAddress>) : ChooseContractUiState
}
