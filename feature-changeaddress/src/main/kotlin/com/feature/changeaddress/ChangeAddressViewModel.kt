package com.feature.changeaddress

import CreateQuoteInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feature.changeaddress.data.AddressInput
import com.feature.changeaddress.data.ChangeAddressRepository
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
              numberCoInsured = moveIntent.numberCoInsured,
              moveFromAddressId = moveIntent.currentHomeAddresses.firstOrNull()?.id,
              isLoading = false,
            )
          }
        },
      )
    }
  }

  fun onSaveNewAddress() {
    _uiState.update { it.copy(isLoading = true) }
    val input = _uiState.value.toCreateQuoteInput()
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

  fun onStreet(street: String) {
    _uiState.update { it.copy(street = street) }
  }

  fun onPostalCode(postalCode: String) {
    _uiState.update { it.copy(postalCode = postalCode) }
  }

  fun onSquareMeters(squareMeters: String) {
    _uiState.update { it.copy(squareMeters = squareMeters) }
  }

  fun onCoInsured(coInsured: Int) {
    _uiState.update { it.copy(numberCoInsured = coInsured) }
  }

  fun onMoveDate(movingDate: LocalDate) {
    _uiState.update { it.copy(movingDate = movingDate) }
  }

  fun onContinue() {
    _uiState.update { it.copy(quotes = emptyList()) }
  }

}

private fun ChangeAddressUiState.toCreateQuoteInput() = CreateQuoteInput(
  moveIntentId = moveIntentId!!,
  address = AddressInput(
    street = street ?: "testersson",
    postalCode = postalCode ?: "",
  ),
  moveFromAddressId = moveFromAddressId!!,
  movingDate = movingDate ?: LocalDate.fromEpochDays(1230),
  numberCoInsured = numberCoInsured ?: 2,
  squareMeters = squareMeters?.toInt() ?: 32,
  apartmentOwnerType = ApartmentOwnerType.RENT,
  isStudent = false,
)
