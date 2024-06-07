package com.hedvig.android.feature.changeaddress.destination.selecthousingtype

import androidx.compose.runtime.Composable
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class SelectHousingTypeViewModel : MoleculeViewModel<SelectHousingTypeEvent, SelectHousingTypeUiState>(
  initialState = SelectHousingTypeUiState(),
  presenter = SelectHousingTypePresenter(),
)

internal class SelectHousingTypePresenter() : MoleculePresenter<SelectHousingTypeEvent, SelectHousingTypeUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectHousingTypeEvent>.present(
    lastState: SelectHousingTypeUiState,
  ): SelectHousingTypeUiState {
    TODO("Not yet implemented")
  }
}

internal data class SelectHousingTypeUiState(
  val errorMessage: String? = null,
  val errorMessageRes: Int? = null, // todo: sure?
)

internal sealed interface SelectHousingTypeEvent {
  data class SelectHousingType(val housingType: HousingType) : SelectHousingTypeEvent

  data object SubmitHousingType : SelectHousingTypeEvent

  data object DismissHousingTypeErrorDialog : SelectHousingTypeEvent

  data object DismissErrorDialog : SelectHousingTypeEvent

  data object ValidateHousingType : SelectHousingTypeEvent
}
