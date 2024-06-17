package com.hedvig.android.feature.changeaddress.destination.entervillainfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.ExtraBuildingType
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.AddExtraBuilding
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.ChangeAncillaryArea
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.ChangeIsSublet
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.ChangeNumberOfBathrooms
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.ChangeYearOfConstruction
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.ClearNavParameters
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.RemoveExtraBuildingClicked
import com.hedvig.android.feature.changeaddress.destination.entervillainfo.EnterVillaInformationEvent.SubmitNewAddress
import com.hedvig.android.feature.changeaddress.navigation.MovingParameters
import com.hedvig.android.feature.changeaddress.navigation.VillaOnlyParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope

internal class EnterVillaInformationViewModel(
  previousParameters: MovingParameters,
) : MoleculeViewModel<EnterVillaInformationEvent, EnterVillaInformationUiState>(
    initialState = EnterVillaInformationUiState(
      extraBuildingTypes = previousParameters.selectHousingTypeParameters.extraBuildingTypes,
    ),
    presenter = EnterVillaInformationPresenter(previousParameters = previousParameters),
  )

internal class EnterVillaInformationPresenter(
  private val previousParameters: MovingParameters,
) : MoleculePresenter<EnterVillaInformationEvent, EnterVillaInformationUiState> {
  @Composable
  override fun MoleculePresenterScope<EnterVillaInformationEvent>.present(
    lastState: EnterVillaInformationUiState,
  ): EnterVillaInformationUiState {
    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        is AddExtraBuilding -> {
          val extraBuildings = currentState.extraBuildings.toMutableList()
          val existingBuildingWithSameId = extraBuildings.find { it.id == event.extraBuilding.id }
          if (existingBuildingWithSameId != null) {
            extraBuildings.remove(existingBuildingWithSameId)
          }
          extraBuildings.add(event.extraBuilding)
          currentState = currentState.copy(extraBuildings = extraBuildings.toList())
        }

        is ChangeAncillaryArea -> {
          currentState = currentState.copy(ancillaryArea = ValidatedInput(event.ancillaryArea))
        }

        is ChangeIsSublet -> currentState = currentState.copy(isSublet = ValidatedInput(event.isSublet))
        is ChangeNumberOfBathrooms ->
          currentState =
            currentState.copy(numberOfBathrooms = ValidatedInput(event.numberOfBathrooms))

        is ChangeYearOfConstruction ->
          currentState =
            currentState.copy(yearOfConstruction = ValidatedInput(event.yearOfConstruction))

        ClearNavParameters -> currentState = currentState.copy(movingParameters = null)

        is RemoveExtraBuildingClicked -> {
          currentState = currentState.copy(extraBuildings = currentState.extraBuildings - event.clickedExtraBuilding)
        }

        SubmitNewAddress -> {
          currentState = currentState.validateHouseInput()
          if (currentState.isHouseInputValid) {
            val villaParams = VillaOnlyParameters(
              yearOfConstruction = currentState.yearOfConstruction.input,
              ancillaryArea = currentState.ancillaryArea.input,
              extraBuildings = currentState.extraBuildings,
              isSublet = currentState.isSublet.input,
              numberOfBathrooms = currentState.numberOfBathrooms.input,
            )
            currentState = currentState.copy(
              movingParameters = previousParameters.copy(villaOnlyParameters = villaParams),
            )
          }
        }
      }
    }
    return currentState
  }
}

internal data class EnterVillaInformationUiState(
  val yearOfConstruction: ValidatedInput<String?> = ValidatedInput(null),
  val ancillaryArea: ValidatedInput<String?> = ValidatedInput(null),
  val numberOfBathrooms: ValidatedInput<String?> = ValidatedInput(null),
  val isSublet: ValidatedInput<Boolean> = ValidatedInput(false),
  val extraBuildingTypes: List<ExtraBuildingType>,
  val extraBuildings: List<ExtraBuilding> = listOf(),
  val isLoading: Boolean = false,
  val movingParameters: MovingParameters? = null,
) {
  val isHouseInputValid: Boolean
    get() {
      return yearOfConstruction.errorMessageRes == null &&
        ancillaryArea.errorMessageRes == null &&
        numberOfBathrooms.errorMessageRes == null
    }

  fun validateHouseInput(): EnterVillaInformationUiState {
    return copy(
      yearOfConstruction = yearOfConstruction.copy(
        errorMessageRes = if (!yearOfConstruction.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_YEAR_OF_CONSTRUCTION_ERROR
        } else {
          null
        },
      ),
      ancillaryArea = ancillaryArea.copy(
        errorMessageRes = if (!ancillaryArea.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_ANCILLARY_AREA_ERROR
        } else {
          null
        },
      ),
      numberOfBathrooms = numberOfBathrooms.copy(
        errorMessageRes = if (!numberOfBathrooms.isPresent) {
          hedvig.resources.R.string.CHANGE_ADDRESS_BATHROOMS_ERROR
        } else {
          null
        },
      ),
    )
  }
}

internal sealed interface EnterVillaInformationEvent {
  data class AddExtraBuilding(val extraBuilding: ExtraBuilding) : EnterVillaInformationEvent

  data class RemoveExtraBuildingClicked(val clickedExtraBuilding: ExtraBuilding) : EnterVillaInformationEvent

  data class ChangeNumberOfBathrooms(val numberOfBathrooms: String) : EnterVillaInformationEvent

  data class ChangeAncillaryArea(val ancillaryArea: String) : EnterVillaInformationEvent

  data class ChangeYearOfConstruction(val yearOfConstruction: String) : EnterVillaInformationEvent

  data class ChangeIsSublet(val isSublet: Boolean) : EnterVillaInformationEvent

  data object SubmitNewAddress : EnterVillaInformationEvent

  data object ClearNavParameters : EnterVillaInformationEvent
}
