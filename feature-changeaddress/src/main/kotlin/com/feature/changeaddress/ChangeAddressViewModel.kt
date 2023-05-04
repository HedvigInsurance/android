package com.feature.changeaddress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.feature.changeaddress.data.ChangeAddressRepository
import com.feature.changeaddress.data.CreateQuoteInput
import com.hedvig.android.core.common.RetryChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

internal class ChangeAddressViewModel(
  private val changeAddressRepository: ChangeAddressRepository,
) : ViewModel() {

  private val _uiState: MutableStateFlow<ChangeAddressUiState> = MutableStateFlow(ChangeAddressUiState())
  private val retryChannel = RetryChannel()
  val uiState: StateFlow<ChangeAddressUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      retryChannel.mapLatest {
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
                isLoading = false,
                numberCoInsured = moveIntent.numberCoInsured,
              )
            }
          },
        )
      }
    }
  }

  fun onSaveNewAddress(input: CreateQuoteInput) {
    _uiState.update { it.copy(isLoading = true) }
    viewModelScope.launch {
      changeAddressRepository.createQuotes(input).fold(
        ifLeft = { error ->
          _uiState.update {
            it.copy(
              isLoading = false,
              errorMessage = error.message
            )
          }
        },
        ifRight = { quotes ->
          _uiState.update {
            it.copy(
              isLoading = false,
              quotes = quotes
            )
          }
        }
      )
    }
  }
}
