package com.hedvig.android.feature.forever

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.raise.either
import com.hedvig.android.apollo.toMonetaryAmount
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.android.feature.forever.data.GetReferralsInformationUseCase
import giraffe.ReferralTermsQuery
import giraffe.ReferralsQuery
import giraffe.fragment.ReferralFragment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.money.MonetaryAmount

internal class ForeverViewModel(
  private val foreverRepository: ForeverRepository,
  private val getReferralTermsUseCase: GetReferralsInformationUseCase,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ForeverUiState())
  val uiState: StateFlow<ForeverUiState> = _uiState

  init {
    loadReferralData()
  }

  private fun loadReferralData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      either {
        val referralsData = foreverRepository.getReferralsData().bind()
        val terms = getReferralTermsUseCase.invoke().bind()
        ForeverUiState(
          referralsData = referralsData,
          referralTerms = terms,
        )
      }.mapLeft {
        ForeverUiState(errorMessage = it.message)
      }.fold(
        ifLeft = { _uiState.value = it },
        ifRight = { _uiState.value = it },
      )
    }
  }

  fun reload() {
    loadReferralData()
  }

  fun onSubmitCode(code: String) {
    viewModelScope.launch {
      _uiState.update {
        it.copy(
          isLoadingCode = true,
          codeError = null,
        )
      }
      foreverRepository.updateCode(code).fold(
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

internal data class ForeverUiState(
  val campaignCode: String? = null,
  val incentive: MonetaryAmount? = null,
  val grossPriceAmount: MonetaryAmount? = null,
  val referralUrl: String? = null,
  val isLoading: Boolean = false,
  val isLoadingCode: Boolean = false,
  val showEditCode: Boolean = false,
  val errorMessage: String? = null,
  val codeError: ForeverRepository.ReferralError? = null,
  val potentialDiscountAmount: MonetaryAmount? = null,
  val currentDiscountAmount: MonetaryAmount? = null,
  val currentNetAmount: MonetaryAmount? = null,
  val referrals: List<Referral> = emptyList(),
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
  ) : this(
    campaignCode = referralsData.referralInformation.campaign.code,
    incentive = referralsData
      .referralInformation
      .campaign
      .incentive
      ?.asMonthlyCostDeduction
      ?.amount
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    grossPriceAmount = referralsData
      .referralInformation
      .costReducedIndefiniteDiscount
      ?.fragments
      ?.costFragment
      ?.monthlyGross
      ?.fragments
      ?.monetaryAmountFragment
      ?.toMonetaryAmount(),
    referralUrl = referralTerms?.url,
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
      ?.toMonetaryAmount()
      ?.negate(),
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
          ?.toMonetaryAmount()
          ?.negate(),
      )
    },
  )
}

private val ReferralFragment.name: String?
  get() {
    asActiveReferral?.name?.let { return it }
    asInProgressReferral?.name?.let { return it }
    asTerminatedReferral?.name?.let { return it }
    return null
  }
