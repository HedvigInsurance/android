package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.android.core.common.RetryChannel
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration.Companion.seconds

class ContractDetailViewModel(
  contractId: String,
  private val getContractDetailsUseCase: GetContractDetailsUseCase,
  hAnalytics: HAnalytics,
) : ViewModel() {
  sealed class ViewState {
    data class Success(val state: ContractDetailViewState) : ViewState()
    object Error : ViewState()
    object Loading : ViewState()
  }

  init {
    hAnalytics.screenView(AppScreen.INSURANCE_DETAIL)
  }

  private val retryChannel = RetryChannel()
  val viewState: StateFlow<ViewState> = retryChannel
    .flatMapLatest {
      getContractDetailsUseCase.invoke(contractId)
        .map { result ->
          result.fold(
            ifLeft = { ViewState.Error },
            ifRight = { ViewState.Success(it) },
          )
        }
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5.seconds),
      ViewState.Loading,
    )

  fun retryLoadingContract() {
    retryChannel.retry()
  }
}
