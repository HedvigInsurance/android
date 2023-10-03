package com.hedvig.app.feature.referrals.ui.redeemcode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.android.QuoteCartId
import com.hedvig.android.data.forever.CampaignCode
import com.hedvig.android.data.forever.ForeverRepository
import com.hedvig.app.feature.offer.usecase.EditCampaignUseCase
import giraffe.RedeemReferralCodeMutation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RedeemCodeViewModel(
  private val quoteCartId: QuoteCartId?,
  private val referralsRepository: ForeverRepository,
  private val editCampaignUseCase: EditCampaignUseCase,
) : ViewModel() {

  data class ViewState(
    val quoteCartId: QuoteCartId? = null,
    val data: RedeemReferralCodeMutation.Data? = null,
    val loading: Boolean = false,
    val errorMessage: String? = null,
  )

  private val _viewState = MutableStateFlow(ViewState())
  val viewState: StateFlow<ViewState> = _viewState.asStateFlow()

  fun redeemReferralCode(code: CampaignCode) {
    viewModelScope.launch {
      if (quoteCartId == null) {
        redeemCode(code)
      } else {
        editQuoteCart(code, quoteCartId)
      }
    }
  }

  private suspend fun editQuoteCart(code: CampaignCode, quoteCartId: QuoteCartId) {
    val state = editCampaignUseCase.addCampaignToQuoteCart(code, quoteCartId)
      .fold(
        ifLeft = { error -> _viewState.value.copy(errorMessage = error.message) },
        ifRight = { id -> _viewState.value.copy(quoteCartId = id) },
      )
    _viewState.update { state }
  }

  private suspend fun redeemCode(code: CampaignCode) {
    val state = referralsRepository.redeemReferralCode(code)
      .fold(
        ifLeft = { error -> _viewState.value.copy(errorMessage = error.message) },
        ifRight = { data -> _viewState.value.copy(data = data) },
      )
    _viewState.update { state }
  }
}
