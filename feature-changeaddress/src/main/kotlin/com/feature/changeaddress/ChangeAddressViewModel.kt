package com.feature.changeaddress

import ApartmentOwnerType
import CreateQuoteInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feature.changeaddress.data.AddressInput
import com.feature.changeaddress.data.ChangeAddressRepository
import com.feature.changeaddress.data.MoveIntentId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

internal class ChangeAddressViewModel(
  private val changeAddressRepository: ChangeAddressRepository,
) : ViewModel() {

  private val _uiState: MutableStateFlow<ChangeAddressUiState> = MutableStateFlow(ChangeAddressUiState())
  val uiState: StateFlow<ChangeAddressUiState> = _uiState.asStateFlow()

  init {
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
              numberCoInsured = ValidatedInput(moveIntent.numberCoInsured),
              moveFromAddressId = moveIntent.currentHomeAddresses.firstOrNull()?.id,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onStreetChanged(street: String) {
    _uiState.update { it.copy(street = ValidatedInput(street)) }
  }

  fun onPostalCodeChanged(postalCode: String) {
    _uiState.update { it.copy(postalCode = ValidatedInput(postalCode)) }
  }

  fun onSquareMetersChanged(squareMeters: String) {
    _uiState.update { it.copy(squareMeters = ValidatedInput(squareMeters)) }
  }

  fun onCoInsuredChanged(coInsured: Int) {
    _uiState.update { it.copy(numberCoInsured = ValidatedInput(coInsured)) }
  }

  fun onMoveDateSelected(movingDate: LocalDate) {
    _uiState.update { it.copy(movingDate = ValidatedInput(movingDate)) }
  }

  fun onSelectHousingType(apartmentOwnerType: ApartmentOwnerType) {
    _uiState.update { it.copy(apartmentOwnerType = ValidatedInput(apartmentOwnerType)) }
  }

  fun onQuotesCleared() {
    _uiState.update { it.copy(quotes = emptyList()) }
  }

  fun onSaveNewAddress() {
    _uiState.update { it.validateInput() }
    if (_uiState.value.isValid) {
      val input = _uiState.value.toCreateQuoteInput()
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
                quotes = quotes,
              )
            }
          },
        )
      }
    }
  }

  fun onAcceptQuote(id: MoveIntentId) {
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

}

private fun ChangeAddressUiState.toCreateQuoteInput() = CreateQuoteInput(
  moveIntentId = moveIntentId!!,
  address = AddressInput(
    street = street.input!!,
    postalCode = postalCode.input!!,
  ),
  moveFromAddressId = moveFromAddressId!!,
  movingDate = movingDate.input!!,
  numberCoInsured = numberCoInsured.input!!,
  squareMeters = squareMeters.input!!.toInt(),
  apartmentOwnerType = apartmentOwnerType.input!!,
  isStudent = false,
)
