package com.hedvig.android.feature.change.tier.ui

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.change.tier.data.CustomizeContractData
import com.hedvig.android.feature.change.tier.data.GetTiersAndDeductiblesUseCase
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectCoverageViewModel(
  insuranceId: String?,
  getTiersAndDeductiblesUseCase: GetTiersAndDeductiblesUseCase,
) : MoleculeViewModel<SelectCoverageEvent, SelectCoverageUiState>(
  initialState = SelectCoverageUiState.Loading,
  presenter = SelectCoveragePresenter(
    insuranceId = insuranceId,
    getTiersAndDeductiblesUseCase = getTiersAndDeductiblesUseCase,
  ),
)

private class SelectCoveragePresenter(
  private val insuranceId: String?,
  private val getTiersAndDeductiblesUseCase: GetTiersAndDeductiblesUseCase,
) : MoleculePresenter<SelectCoverageEvent, SelectCoverageUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectCoverageEvent>.present(lastState: SelectCoverageUiState)
    : SelectCoverageUiState {
    TODO("Not yet implemented")
  }
}

internal sealed interface SelectCoverageEvent {
}

internal sealed interface SelectCoverageUiState {
  data object Loading : SelectCoverageUiState
  data class Success(val data: CustomizeContractData) : SelectCoverageUiState
  data object Failure : SelectCoverageUiState
}
