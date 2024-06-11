package com.hedvig.android.feature.changeaddress.destination.selecthousingtype

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeEvent.ClearNavigationParameters
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeEvent.DismissErrorDialog
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeEvent.DismissHousingTypeErrorDialog
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeEvent.SelectHousingType
import com.hedvig.android.feature.changeaddress.destination.selecthousingtype.SelectHousingTypeEvent.SubmitHousingType
import com.hedvig.android.feature.changeaddress.navigation.SelectHousingTypeParameters
import com.hedvig.android.molecule.android.MoleculeViewModel
import com.hedvig.android.molecule.public.MoleculePresenter
import com.hedvig.android.molecule.public.MoleculePresenterScope
import hedvig.resources.R

internal class SelectHousingTypeViewModel(changeAddressRepository: ChangeAddressRepository) :
  MoleculeViewModel<SelectHousingTypeEvent, SelectHousingTypeUiState>(
    initialState = SelectHousingTypeUiState(),
    presenter = SelectHousingTypePresenter(changeAddressRepository),
  )

internal class SelectHousingTypePresenter(private val changeAddressRepository: ChangeAddressRepository) :
  MoleculePresenter<SelectHousingTypeEvent, SelectHousingTypeUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectHousingTypeEvent>.present(
    lastState: SelectHousingTypeUiState,
  ): SelectHousingTypeUiState {
    var dataLoadIteration by remember {
      mutableIntStateOf(0)
    }

    var currentState by remember {
      mutableStateOf(lastState)
    }

    CollectEvents { event ->
      when (event) {
        DismissErrorDialog -> currentState = currentState.copy(errorMessage = null)

        DismissHousingTypeErrorDialog -> currentState = currentState.copy(errorMessageRes = null)

        is SelectHousingType -> {
          currentState = currentState.copy(housingType = ValidatedInput(input = event.housingType))
        }

        SubmitHousingType -> {
          if (!currentState.housingType.isPresent) {
            currentState = currentState.copy(
              errorMessageRes = R.string.CHANGE_ADDRESS_HOUSING_TYPE_ERROR,
            )
          } else {
            dataLoadIteration++
          }
        }

        ClearNavigationParameters -> currentState = currentState.copy(navigationParameters = null)
      }
    }

    LaunchedEffect(dataLoadIteration) {
      if (dataLoadIteration > 0) {
        currentState = currentState.copy(isLoading = true)
        changeAddressRepository.createMoveIntent().fold(
          ifLeft = { error ->
            currentState = currentState.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          },
          ifRight = { moveIntent ->
            val parameters = SelectHousingTypeParameters(
              minDate = moveIntent.movingDateRange.start,
              maxDate = moveIntent.movingDateRange.endInclusive,
              moveIntentId = moveIntent.id.id,
              suggestedNumberInsured = moveIntent.suggestedNumberInsured.toString(),
              moveFromAddressId = moveIntent.currentHomeAddresses.firstOrNull()?.id,
              extraBuildingTypes = moveIntent.extraBuildingTypes,
              isEligibleForStudent = moveIntent.isApartmentAvailableforStudent == true &&
                currentState.housingType.input != HousingType.VILLA,
              maxSquareMeters = when (currentState.housingType.input) {
                HousingType.APARTMENT_RENT,
                HousingType.APARTMENT_OWN,
                -> moveIntent.maxApartmentSquareMeters

                HousingType.VILLA -> moveIntent.maxHouseSquareMeters
                null -> null
              },
              maxNumberCoInsured = when (currentState.housingType.input) { // todo: is this number of coInsured or just Insured (incl.main insured?) bc later it's used as if it is the later one.
                HousingType.APARTMENT_RENT,
                HousingType.APARTMENT_OWN,
                -> moveIntent.maxApartmentNumberCoInsured

                HousingType.VILLA -> moveIntent.maxHouseNumberCoInsured
                null -> null
              },
              housingType = currentState.housingType.input,
            )
            currentState = currentState.copy(
              isLoading = false,
              navigationParameters = parameters,
            )
          },
        )
      }
    }
    return currentState
  }
}

internal data class SelectHousingTypeUiState(
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val housingType: ValidatedInput<HousingType?> = ValidatedInput(null),
  val errorMessageRes: Int? = null,
  val navigationParameters: SelectHousingTypeParameters? = null,
)

internal sealed interface SelectHousingTypeEvent {
  data class SelectHousingType(val housingType: HousingType) : SelectHousingTypeEvent

  data object SubmitHousingType : SelectHousingTypeEvent

  data object DismissHousingTypeErrorDialog : SelectHousingTypeEvent

  data object DismissErrorDialog : SelectHousingTypeEvent

  data object ClearNavigationParameters : SelectHousingTypeEvent
}
