package com.hedvig.app.feature.insurance.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

  private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
  val viewState = _viewState.asStateFlow()

  init {
    hAnalytics.screenView(AppScreen.INSURANCE_DETAIL)
    loadContract(contractId)
  }

  fun loadContract(id: String) {
    viewModelScope.launch {
      val viewState = when (val insurance = getContractDetailsUseCase.invoke(id)) {
        is Either.Left -> ViewState.Error
        is Either.Right -> ViewState.Success(insurance.value)
      }
      _viewState.value = viewState
    }
  }
}
