package com.hedvig.app.feature.profile.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.language.LanguageService
import com.hedvig.app.feature.offer.usecase.CampaignCode
import com.hedvig.app.feature.profile.data.PaymentMethod
import com.hedvig.app.feature.referrals.data.RedeemReferralCodeRepository
import com.hedvig.app.util.apollo.format
import giraffe.type.PayoutMethodStatus
import java.time.LocalDate
import java.util.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentViewModel(
  private val redeemReferralCodeRepository: RedeemReferralCodeRepository,
  private val paymentRepository: PaymentRepository,
  private val languageService: LanguageService,
) : ViewModel() {

  private val _uiState = MutableStateFlow(PaymentUiState())
  val uiState: StateFlow<PaymentUiState> = _uiState

  data class PaymentUiState(
    val nextChargeAmount: String? = null,
    val nextChargeDate: LocalDate? = null,
    val monthlyCost: String? = null,
    val insuranceCosts: List<InsuranceCost> = emptyList(),
    val totalDiscount: String? = null,
    val activeDiscounts: List<Discount> = emptyList(),
    val paymentMethod: PaymentMethod? = null,
    val discountCode: CampaignCode? = null,
    val discountError: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = false,
    val payoutStatus: PayoutStatus? = null
  ) {
    data class InsuranceCost(
      val displayName: String,
      val cost: String?,
    )

    data class PaymentMethod(
      val displayName: String?,
      val displayValue: String?,
    )

    data class Discount(
      val code: String,
      val displayName: String,
    )

    enum class PayoutStatus {
      ACTIVE,
      PENDING,
      NEEDS_SETUP;
    }
  }

  init {
    loadPaymentData()
  }

  private fun loadPaymentData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      paymentRepository.getPaymentData().fold(
        ifLeft = { error -> _uiState.update { it.copy(errorMessage = error.message, isLoading = false) } },
        ifRight = { paymentData -> _uiState.value = paymentData.toUiState(languageService.getLocale()) },
      )
    }
  }

  fun onDiscountCodeChanged(code: CampaignCode) {
    _uiState.update { it.copy(discountCode = code, discountError = null) }
  }

  fun onDiscountCodeAdded() {
    viewModelScope.launch {
      val code = _uiState.value.discountCode ?: return@launch
      redeemReferralCodeRepository.redeemReferralCode(code)
        .fold(
          ifLeft = { error -> _uiState.update { it.copy(discountError = error.message) } },
          ifRight = { data -> loadPaymentData() },
        )
    }
  }

  fun retry() {
    loadPaymentData()
  }
}

private fun PaymentRepository.PaymentData.toUiState(locale: Locale) = PaymentViewModel.PaymentUiState(
  nextChargeAmount = nextCharge.format(locale),
  monthlyCost = monthlyCost?.format(locale),
  nextChargeDate = nextChargeDate,
  insuranceCosts = contracts.map {
    PaymentViewModel.PaymentUiState.InsuranceCost(
      displayName = it,
      cost = null,
    )
  },
  totalDiscount = totalDiscount?.negate()?.format(locale),
  activeDiscounts = redeemedCampagins.map {
    PaymentViewModel.PaymentUiState.Discount(
      code = it.code,
      displayName = it.displayValue ?: "-",
    )
  },
  paymentMethod = PaymentViewModel.PaymentUiState.PaymentMethod(
    displayName = when (paymentMethod) {
      is PaymentMethod.CardPaymentMethod -> paymentMethod.brand ?: "Unknown"
      is PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.name
      null -> null
    },
    displayValue = when (paymentMethod) {
      is PaymentMethod.CardPaymentMethod -> paymentMethod.lastFourDigits
      is PaymentMethod.ThirdPartyPaymentMethd -> paymentMethod.type
      null -> null
    },
  ),
  payoutStatus = when (payoutMethodStatus) {
    PayoutMethodStatus.ACTIVE -> PaymentViewModel.PaymentUiState.PayoutStatus.ACTIVE
    PayoutMethodStatus.PENDING ->  PaymentViewModel.PaymentUiState.PayoutStatus.PENDING
    PayoutMethodStatus.NEEDS_SETUP ->  PaymentViewModel.PaymentUiState.PayoutStatus.NEEDS_SETUP
    PayoutMethodStatus.UNKNOWN__ -> null
    null -> null
  }
)
