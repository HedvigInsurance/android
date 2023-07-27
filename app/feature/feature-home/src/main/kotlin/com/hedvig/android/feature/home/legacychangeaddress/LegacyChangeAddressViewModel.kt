package com.hedvig.android.feature.home.legacychangeaddress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

internal class LegacyChangeAddressViewModel(
  private val getUpcomingAgreement: GetUpcomingAgreementUseCase,
  private val addressChangeStoryId: GetAddressChangeStoryIdUseCase,
  hAnalytics: HAnalytics,
) : ViewModel() {

  protected val _viewState = MutableLiveData<ViewState>()
  val viewState: LiveData<ViewState>
    get() = _viewState

  init {
    fetchDataAndCreateState()
    hAnalytics.screenView(AppScreen.MOVING_FLOW_INTRO)
  }

  private fun fetchDataAndCreateState() {
    _viewState.postValue(ViewState.Loading)
    viewModelScope.launch {
      _viewState.postValue(createViewState())
    }
  }

  private suspend fun createViewState(): ViewState {
    return getUpComingAgreementState(
      onNoUpcomingChange = ::getSelfChangeState,
    )
  }

  private suspend fun getUpComingAgreementState(onNoUpcomingChange: suspend () -> ViewState): ViewState {
    return when (val upcomingAgreement = getUpcomingAgreement.invoke()) {
      is GetUpcomingAgreementUseCase.UpcomingAgreementResult.NoUpcomingAgreementChange -> onNoUpcomingChange()
      is GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement -> ViewState.ChangeAddressInProgress(
        upcomingAgreement,
      )
      is GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error -> ViewState.UpcomingAgreementError(
        upcomingAgreement,
      )
    }
  }

  private suspend fun getSelfChangeState() = when (val selfChangeEligibility = addressChangeStoryId.invoke()) {
    is GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Eligible -> {
      ViewState.SelfChangeAddress(selfChangeEligibility.embarkStoryId)
    }
    is GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Blocked -> ViewState.ManualChangeAddress
    is GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Error -> {
      ViewState.SelfChangeError(selfChangeEligibility)
    }
  }

  fun reload() {
    fetchDataAndCreateState()
  }
}

internal sealed class ViewState {
  object Loading : ViewState()
  data class SelfChangeAddress(val embarkStoryId: String) : ViewState()
  object ManualChangeAddress : ViewState()
  data class UpcomingAgreementError(val error: GetUpcomingAgreementUseCase.UpcomingAgreementResult.Error) : ViewState()
  data class SelfChangeError(val error: GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult.Error) : ViewState()
  data class ChangeAddressInProgress(
    val upcomingAgreementResult: GetUpcomingAgreementUseCase.UpcomingAgreementResult.UpcomingAgreement,
  ) : ViewState()
}
