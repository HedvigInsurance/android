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
import com.hedvig.android.feature.changeaddress.data.MoveIntent
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
    initialState = SelectHousingTypeUiState.Loading,
    presenter = SelectHousingTypePresenter(changeAddressRepository),
  )

internal class SelectHousingTypePresenter(private val changeAddressRepository: ChangeAddressRepository) :
  MoleculePresenter<SelectHousingTypeEvent, SelectHousingTypeUiState> {
  @Composable
  override fun MoleculePresenterScope<SelectHousingTypeEvent>.present(
    lastState: SelectHousingTypeUiState,
  ): SelectHousingTypeUiState {
    var createMoveIntentIteration by remember {
      mutableIntStateOf(0)
    }

    var currentState by remember {
      mutableStateOf(lastState)
    }
    var submittingHousingType by remember { mutableStateOf<HousingType?>(null) }

    CollectEvents { event ->

      when (event) {
        DismissErrorDialog -> {
          createMoveIntentIteration++
        }

        DismissHousingTypeErrorDialog -> {
          val state = currentState as? SelectHousingTypeUiState.Content ?: return@CollectEvents
          currentState = state.copy(errorMessageRes = null)
        }

        is SelectHousingType -> {
          val state = currentState as? SelectHousingTypeUiState.Content ?: return@CollectEvents
          currentState = state.copy(housingType = ValidatedInput(input = event.housingType))
        }

        SubmitHousingType -> {
          val state = currentState as? SelectHousingTypeUiState.Content ?: return@CollectEvents
          if (!state.housingType.isPresent) {
            currentState = state.copy(
              errorMessageRes = R.string.CHANGE_ADDRESS_HOUSING_TYPE_ERROR,
            )
          } else {
            submittingHousingType = state.housingType.input
          }
        }

        ClearNavigationParameters -> {
          val state = currentState as? SelectHousingTypeUiState.Content ?: return@CollectEvents
          currentState = state.copy(navigationParameters = null)
        }
      }
    }

    LaunchedEffect(createMoveIntentIteration) {
      currentState = SelectHousingTypeUiState.Loading
      changeAddressRepository.createMoveIntent().fold(
        ifLeft = { error ->
          currentState = SelectHousingTypeUiState.Error(error.message)
        },
        ifRight = { moveIntent ->
          currentState = SelectHousingTypeUiState.Content(
            moveIntent = moveIntent,
            oldHomeInsuranceDuration = moveIntent.oldAddressCoverageDurationDays,
            housingType = ValidatedInput(null),
            errorMessageRes = null,
            navigationParameters = null,
            isButtonLoading = false,
          )
        },
      )
    }

    LaunchedEffect(submittingHousingType) {
      if (submittingHousingType != null) {
        val state = currentState as? SelectHousingTypeUiState.Content ?: return@LaunchedEffect
        currentState = state.copy(isButtonLoading = true)
        val moveIntent = state.moveIntent
        val parameters = SelectHousingTypeParameters(
          minDate = moveIntent.movingDateRange.start,
          maxDate = moveIntent.movingDateRange.endInclusive,
          moveIntentId = moveIntent.id.id,
          suggestedNumberInsured = moveIntent.suggestedNumberInsured.toString(),
          moveFromAddressId = moveIntent.currentHomeAddresses.firstOrNull()?.id,
          extraBuildingTypes = moveIntent.extraBuildingTypes,
          isEligibleForStudent = moveIntent.isApartmentAvailableforStudent == true &&
            state.housingType.input != HousingType.VILLA,
          maxSquareMeters = when (state.housingType.input) {
            HousingType.APARTMENT_RENT,
            HousingType.APARTMENT_OWN,
            -> moveIntent.maxApartmentSquareMeters

            HousingType.VILLA -> moveIntent.maxHouseSquareMeters
            null -> null
          },
          maxNumberCoInsured = when (state.housingType.input) {
            HousingType.APARTMENT_RENT,
            HousingType.APARTMENT_OWN,
            -> moveIntent.maxApartmentNumberCoInsured

            HousingType.VILLA -> moveIntent.maxHouseNumberCoInsured
            null -> null
          },
          housingType = state.housingType.input,
          oldAddressCoverageDurationDays = state.oldHomeInsuranceDuration,
        )
        currentState = state.copy(
          navigationParameters = parameters,
          isButtonLoading = false,
        )
        submittingHousingType = null
      }
    }

    return currentState
  }
}

internal sealed interface SelectHousingTypeUiState {
  data object Loading : SelectHousingTypeUiState

  data class Error(val errorMessage: String?) : SelectHousingTypeUiState

  data class Content(
    val moveIntent: MoveIntent,
    val oldHomeInsuranceDuration: Int?,
    val housingType: ValidatedInput<HousingType?> = ValidatedInput(null),
    val errorMessageRes: Int? = null,
    val navigationParameters: SelectHousingTypeParameters? = null,
    val isButtonLoading: Boolean = false,
  ) : SelectHousingTypeUiState
}

internal sealed interface SelectHousingTypeEvent {
  data class SelectHousingType(val housingType: HousingType) : SelectHousingTypeEvent

  data object SubmitHousingType : SelectHousingTypeEvent

  data object DismissHousingTypeErrorDialog : SelectHousingTypeEvent

  data object DismissErrorDialog : SelectHousingTypeEvent

  data object ClearNavigationParameters : SelectHousingTypeEvent
}
