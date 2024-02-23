package com.hedvig.android.feature.changeaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.ui.ValidatedInput
import com.hedvig.android.feature.changeaddress.data.AddressInput
import com.hedvig.android.feature.changeaddress.data.ChangeAddressRepository
import com.hedvig.android.feature.changeaddress.data.ExtraBuilding
import com.hedvig.android.feature.changeaddress.data.HousingType
import com.hedvig.android.feature.changeaddress.data.MoveIntentId
import com.hedvig.android.feature.changeaddress.data.MoveQuote
import com.hedvig.android.feature.changeaddress.data.QuoteInput
import com.hedvig.android.language.LanguageService
import hedvig.resources.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class ChangeAddressViewModel(
  private val changeAddressRepository: ChangeAddressRepository,
  private val languageService: LanguageService,
) : ViewModel() {
  private val _uiState: MutableStateFlow<ChangeAddressUiState> = MutableStateFlow(ChangeAddressUiState())
  val uiState: StateFlow<ChangeAddressUiState> = _uiState.asStateFlow()

  fun onStreetChanged(street: String) {
    _uiState.update { it.copy(street = ValidatedInput(street)) }
  }

  fun onPostalCodeChanged(postalCode: String) {
    _uiState.update { it.copy(postalCode = ValidatedInput(postalCode)) }
  }

  fun onSquareMetersChanged(squareMeters: String) {
    _uiState.update { it.copy(squareMeters = ValidatedInput(squareMeters)) }
  }

  fun onCoInsuredChanged(coInsured: String) {
    _uiState.update { it.copy(numberInsured = ValidatedInput(coInsured)) }
  }

  fun onYearOfConstructionChanged(yearOfConstruction: String) {
    _uiState.update { it.copy(yearOfConstruction = ValidatedInput(yearOfConstruction)) }
  }

  fun onAncillaryAreaChanged(ancillaryArea: String) {
    _uiState.update { it.copy(ancillaryArea = ValidatedInput(ancillaryArea)) }
  }

  fun onNumberOfBathroomsChanged(numberOfBathrooms: String) {
    _uiState.update { it.copy(numberOfBathrooms = ValidatedInput(numberOfBathrooms)) }
  }

  fun onMoveDateSelected(movingDate: LocalDate) {
    _uiState.update { it.copy(movingDate = ValidatedInput(movingDate)) }
  }

  fun onIsStudentChanged(isStudent: Boolean) {
    _uiState.update { it.copy(isStudent = isStudent) }
  }

  fun onIsSubletChanged(isSublet: Boolean) {
    _uiState.update { it.copy(isSublet = ValidatedInput(isSublet)) }
  }

  fun validateAddressInput(): Boolean {
    _uiState.update { it.validateAddressInput() }
    return _uiState.value.isAddressInputValid
  }

  fun validateHouseInput(): Boolean {
    _uiState.update { it.validateHouseInput() }
    return _uiState.value.isHouseInputValid
  }

  /**
   * After we've received the MoveIntentId, `startMovingFlowAfterHavingReceivedMoveIntentId` is used to trigger the
   * navigation to the next question. When this function is called, it means the navigation event has been handled.
   */
  fun onNavigatedToFirstStepAfterHavingReceivedMoveIntentId() {
    _uiState.update {
      it.copy(navigateToFirstStepAfterHavingReceivedMoveIntentId = false)
    }
  }

  /**
   * After we've received the offer quotes, `navigateToOfferScreenAfterHavingReceivedQuotes` is used to trigger the
   * navigation to the offer page. When this function is called, it means the navigation event has been handled.
   */
  fun onNavigatedToOfferScreenAfterHavingReceivedQuotes() {
    _uiState.update {
      it.copy(navigateToOfferScreenAfterHavingReceivedQuotes = false)
    }
  }

  fun onSubmitNewAddress() {
    if (uiState.value.moveIntentId == null) {
      _uiState.update {
        it.copy(errorMessage = "No MoveIntent found")
      }
    }

    val input = _uiState.value.toQuoteInput()
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      changeAddressRepository.createQuotes(input).fold(
        ifLeft = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          }
        },
        ifRight = { quotes ->
          _uiState.update {
            it.copy(
              isLoading = false,
              navigateToOfferScreenAfterHavingReceivedQuotes = true,
              quotes = quotes,
            )
          }
        },
      )
    }
  }

  fun onExpandQuote(moveQuote: MoveQuote) {
    _uiState.update { uiState: ChangeAddressUiState ->
      uiState.copy(
        quotes = uiState.quotes.map { quote: MoveQuote ->
          if (quote == moveQuote) moveQuote.copy(isExpanded = !moveQuote.isExpanded) else quote
        },
      )
    }
  }

  fun onConfirmMove(id: MoveIntentId) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      changeAddressRepository.commitMove(id).fold(
        ifLeft = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          }
        },
        ifRight = { result ->
          _uiState.update {
            it.copy(
              isLoading = false,
              successfulMoveResult = result,
            )
          }
        },
      )
    }
  }

  fun onErrorDialogDismissed() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  fun onValidateHousingType() {
    if (_uiState.value.housingType.input == null) {
      _uiState.update {
        it.copy(
          housingType = ValidatedInput(
            input = null,
            errorMessageRes = R.string.CHANGE_ADDRESS_HOUSING_TYPE_ERROR,
          ),
        )
      }
    }
  }

  fun onHousingTypeSelected(housingType: HousingType) {
    _uiState.update { it.copy(housingType = ValidatedInput(housingType)) }
  }

  fun onHousingTypeErrorDialogDismissed() {
    _uiState.update { it.copy(housingType = it.housingType.copy(errorMessageRes = null)) }
  }

  fun onHousingTypeSubmitted() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      changeAddressRepository.createMoveIntent().fold(
        ifLeft = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.message,
            )
          }
        },
        ifRight = { moveIntent ->
          _uiState.update {
            it.copy(
              moveIntentId = moveIntent.id,
              navigateToFirstStepAfterHavingReceivedMoveIntentId = true,
              numberInsured = ValidatedInput(moveIntent.suggestedNumberInsured.toString()),
              moveFromAddressId = moveIntent.currentHomeAddresses.firstOrNull()?.id,
              extraBuildingTypes = moveIntent.extraBuildingTypes,
              isEligibleForStudent = moveIntent.isApartmentAvailableforStudent == true &&
                uiState.value.housingType.input != HousingType.VILLA,
              maxSquareMeters = when (uiState.value.housingType.input) {
                HousingType.APARTMENT_RENT,
                HousingType.APARTMENT_OWN,
                -> moveIntent.maxApartmentSquareMeters

                HousingType.VILLA -> moveIntent.maxHouseSquareMeters
                null -> null
              },
              maxNumberCoInsured = when (uiState.value.housingType.input) {
                HousingType.APARTMENT_RENT,
                HousingType.APARTMENT_OWN,
                -> moveIntent.maxApartmentNumberCoInsured

                HousingType.VILLA -> moveIntent.maxHouseNumberCoInsured
                null -> null
              },
              isLoading = false,
              datePickerUiState = DatePickerUiState(
                locale = languageService.getLocale(),
                initiallySelectedDate = null,
                minDate = moveIntent.movingDateRange.start,
                maxDate = moveIntent.movingDateRange.endInclusive,
              ),
            )
          }
        },
      )
    }
  }

  fun onRemoveExtraBuildingClicked(clickedExtraBuilding: ExtraBuilding) {
    _uiState.update {
      it.copy(extraBuildings = it.extraBuildings - clickedExtraBuilding)
    }
  }

  fun addExtraBuilding(extraBuilding: ExtraBuilding) {
    _uiState.update {
      val extraBuildings = it.extraBuildings.toMutableList()
      val existingBuildingWithSameId = extraBuildings.find { it.id == extraBuilding.id }
      if (existingBuildingWithSameId != null) {
        extraBuildings.remove(existingBuildingWithSameId)
      }
      extraBuildings.add(extraBuilding)
      it.copy(extraBuildings = extraBuildings.toList())
    }
  }
}

private fun ChangeAddressUiState.toQuoteInput() = when (housingType.input) {
  HousingType.APARTMENT_RENT,
  HousingType.APARTMENT_OWN,
  -> QuoteInput.ApartmentInput(
    moveIntentId = moveIntentId!!,
    address = AddressInput(
      street = street.input!!,
      postalCode = postalCode.input!!,
    ),
    moveFromAddressId = moveFromAddressId!!,
    movingDate = movingDate.input!!,
    numberCoInsured = numberInsured.input!!.toInt() - 1,
    squareMeters = squareMeters.input!!.toInt(),
    apartmentOwnerType = housingType.input!!,
    isStudent = isStudent,
  )

  HousingType.VILLA -> QuoteInput.VillaInput(
    moveIntentId = moveIntentId!!,
    address = AddressInput(
      street = street.input!!,
      postalCode = postalCode.input!!,
    ),
    moveFromAddressId = moveFromAddressId!!,
    movingDate = movingDate.input!!,
    numberCoInsured = numberInsured.input!!.toInt() - 1,
    squareMeters = squareMeters.input!!.toInt(),
    apartmentOwnerType = housingType.input!!,
    yearOfConstruction = yearOfConstruction.input!!.toInt(),
    ancillaryArea = ancillaryArea.input!!.toInt(),
    numberOfBathrooms = numberOfBathrooms.input!!.toInt(),
    extraBuildings = extraBuildings,
    isStudent = isStudent,
    isSubleted = isSublet.input,
  )

  null -> throw IllegalArgumentException("No housing type found when creating input")
}
