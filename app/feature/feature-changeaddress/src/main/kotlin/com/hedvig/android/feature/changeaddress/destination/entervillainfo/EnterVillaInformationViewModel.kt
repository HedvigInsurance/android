package com.hedvig.android.feature.changeaddress.destination.entervillainfo

import androidx.compose.runtime.Composable
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class EnterVillaInformationViewModel() : MoleculeViewModel<EnterVillaInformationEvent, EnterVillaInformationUiState>(
  initialState = EnterVillaInformationUiState(), // todo: parse initial parameters?
  presenter = EnterVillaInformationPresenter(),
)

internal class EnterVillaInformationPresenter() : MoleculePresenter<EnterVillaInformationEvent, EnterVillaInformationUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterVillaInformationEvent>.present(
    lastState: EnterVillaInformationUiState,
  ): EnterVillaInformationUiState {
    TODO("Not yet implemented")
  }
}

internal data class EnterVillaInformationUiState(
  val yearOfConstruction: ValidatedInput<String?> = ValidatedInput(null),
  val ancillaryArea: ValidatedInput<String?> = ValidatedInput(null),
  val numberOfBathrooms: ValidatedInput<String?> = ValidatedInput(null),
  val isSublet: ValidatedInput<Boolean> = ValidatedInput(false),
  val errorMessage: String? = null,
  val extraBuildingTypes: List<ExtraBuildingType> = emptyList(),
  val extraBuildings: List<ExtraBuilding> = listOf(),
  val isLoading: Boolean = false, // todo: button here
)

internal sealed interface EnterVillaInformationEvent {
  data class AddExtraBuilding(val extraBuilding: ExtraBuilding) : EnterVillaInformationEvent

  data class RemoveExtraBuildingClicked(val clickedExtraBuilding: ExtraBuilding) : EnterVillaInformationEvent

  data object DismissErrorDialog : EnterVillaInformationEvent

  data class ChangeNumberOfBathrooms(val numberOfBathrooms: String) : EnterVillaInformationEvent

  data class ChangeAncillaryArea(val ancillaryArea: String) : EnterVillaInformationEvent

  data class ChangeYearOfConstruction(val yearOfConstruction: String) : EnterVillaInformationEvent

  data class ChangeIsSublet(val isSublet: Boolean) : EnterVillaInformationEvent

  data object SubmitNewAddress : EnterVillaInformationEvent

  data object ValidateHouseInput : EnterVillaInformationEvent
}
