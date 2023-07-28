package com.hedvig.android.feature.forever

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.apollo.toMonetaryAmount
import com.hedvig.android.language.LanguageService
import com.hedvig.android.feature.forever.data.ReferralsRepository
import giraffe.ReferralTermsQuery
import giraffe.ReferralsQuery
import giraffe.fragment.ReferralFragment
import java.util.*
import javax.money.MonetaryAmount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReferralsViewModel(
  private val referralsRepository: ReferralsRepository,
  private val getReferralTermsUseCase: GetReferralsInformationUseCase,
  private val languageService: LanguageService,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ReferralsUiState())
  val uiState: StateFlow<ReferralsUiState> = _uiState

  init {
    loadReferralData()
  }

  private fun loadReferralData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val referralsData = referralsRepository.getReferralsData().bind()
        val terms = getReferralTermsUseCase.invoke().bind()
        ReferralsUiState(
          referralsData = referralsData,
          referralTerms = terms,
          locale = languageService.getLocale(),
        )
      }.mapLeft {
        ReferralsUiState(errorMessage = it.message)
      }.fold(
        ifLeft = { _uiState.value = it },
        ifRight = { _uiState.value = it },
      )
    }
  }

  fun reload() {
    loadReferralData()
  }

  fun onCodeChanged(campaignCode: String?) {
    _uiState.update {
      it.copy(
        editedCampaignCode = campaignCode,
        codeError = null,
      )
    }
  }

  fun onSubmitCode(code: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          isLoadingCode = true,
          codeError = null,
        )
      }
      referralsRepository.updateCode(code).fold(
        ifLeft = { referralError ->
          _uiState.update {
            it.copy(
              codeError = referralError,
              isLoadingCode = false,
            )
          }
        },
        ifRight = { code ->
          _uiState.update {
            it.copy(
              campaignCode = code,
              isLoadingCode = false,
              showEditCode = false,
            )
          }
        },
      )
    }
  }
}

data class ReferralsUiState(
  val campaignCode: String? = null,
  val editedCampaignCode: String? = null,
  val incentive: MonetaryAmount? = null,
  val grossPriceAmount: MonetaryAmount? = null,
  val referralUrl: String? = null,
  val isLoading: Boolean = false,
  val isLoadingCode: Boolean = false,
  val showEditCode: Boolean = false,
  val errorMessage: String? = null,
  val codeError: ReferralsRepository.ReferralError? = null,
  val potentialDiscountAmount: MonetaryAmount? = null,
  val currentDiscountAmount: MonetaryAmount? = null,
  val currentNetAmount: MonetaryAmount? = null,
  val referrals: List<Referral> = emptyList(),
  val locale: Locale? = null,
) {

  data class Referral(
    val name: String?,
    val state: ReferralState,
    val discount: MonetaryAmount?,
  )

  enum class ReferralState {
    ACTIVE, IN_PROGRESS, TERMINATED, UNKNOWN
  }

  constructor(
    referralsData: ReferralsQuery.Data,
    referralTerms: ReferralTermsQuery.ReferralTerms?,
    locale: Locale,
  ) : this(
    incentive = referralsData
      .referralInformation
      .campaign
      .incentive
      ?.asMonthlyCostDeduction
      ?.amount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    referralUrl = referralTerms?.url,
    campaignCode = referralsData.referralInformation.campaign.code,
    editedCampaignCode = referralsData.referralInformation.campaign.code,
    grossPriceAmount = referralsData
      .chargeEstimation
      .subscription
      .fragments
      .monetaryAmountFragment
      .toMonetaryAmount(),
    potentialDiscountAmount = referralsData
      .referralInformation
      .campaign
      .incentive
      ?.asMonthlyCostDeduction
      ?.amount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    currentDiscountAmount = referralsData
      .referralInformation
      .costReducedIndefiniteDiscount
      ?.fragments
      ?.costFragment
      ?.monthlyDiscount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    currentNetAmount = referralsData
      .referralInformation
      .costReducedIndefiniteDiscount
      ?.fragments
      ?.costFragment
      ?.monthlyNet
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    referrals = referralsData.referralInformation.invitations.map {
      Referral(
        name = it.fragments.referralFragment.name,
        state = when {
          it.fragments.referralFragment.asInProgressReferral != null -> ReferralState.IN_PROGRESS
          it.fragments.referralFragment.asActiveReferral != null -> ReferralState.ACTIVE
          it.fragments.referralFragment.asTerminatedReferral != null -> ReferralState.TERMINATED
          else -> ReferralState.UNKNOWN
        },
        discount = it.fragments
          .referralFragment
          .asActiveReferral
          ?.discount
          ?.fragments
          ?.monetaryAmountFragment
          ?.toMonetaryAmount(),
      )
    },
    locale = locale,
  )
}

private val ReferralFragment.name: String?
  get() {
    asActiveReferral?.name?.let { return it }
    asInProgressReferral?.name?.let { return it }
    asTerminatedReferral?.name?.let { return it }
    return null
  }
