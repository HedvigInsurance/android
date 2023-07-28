package com.hedvig.android.feature.profile.payment.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.apollo.format
import com.hedvig.android.feature.profile.payment.PaymentRepository
import com.hedvig.android.language.LanguageService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.Locale

class PaymentHistoryViewModel(
  private val paymentRepository: PaymentRepository,
  val languageService: LanguageService,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PaymentHistoryUiState())
  val uiState: StateFlow<PaymentHistoryUiState> = _uiState

  data class PaymentHistoryUiState(
    val charges: List<Payment> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
  ) {
    data class Payment(
      val amount: String,
      val date: LocalDate,
    )
  }

  init {
    loadPaymentHistory()
  }

  private fun loadPaymentHistory() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      paymentRepository.getChargeHistory().fold(
        ifLeft = { _uiState.update { it.copy(errorMessage = it.errorMessage) } },
        ifRight = { _uiState.value = it.toUiState(languageService.getLocale()) },
      )
    }
  }
}

private fun PaymentRepository.ChargeHistory.toUiState(locale: Locale) = PaymentHistoryViewModel.PaymentHistoryUiState(
  charges = charges.map {
    PaymentHistoryViewModel.PaymentHistoryUiState.Payment(
      amount = it.amount.format(locale),
      date = it.date,
    )
  },
)
