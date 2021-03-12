package com.hedvig.app.feature.home.ui.changeaddress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult
import kotlinx.coroutines.launch

class ChangeAddressViewModel(
    private val getUpcomingAgreement: GetUpcomingAgreementUseCase,
    private val getSelfChangeEligibility: GetSelfChangeEligibilityUseCase,
) : ViewModel() {

    val viewState: LiveData<ViewState>
        get() = _viewState

    private val _viewState = MutableLiveData<ViewState>()

    init {
        fetchDataAndCreateState()
    }

    private fun fetchDataAndCreateState() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            _viewState.value = createViewState()
        }
    }

    private suspend fun createViewState(): ViewState {
        return getUpComingAgreementState(
            onNoUpcomingChange = ::getSelfChangeState
        )
    }

    private suspend fun getUpComingAgreementState(onNoUpcomingChange: suspend () -> ViewState) = when (val upcomingAgreement = getUpcomingAgreement()) {
        is UpcomingAgreementResult.NoUpcomingAgreementChange -> onNoUpcomingChange()
        is UpcomingAgreementResult.UpcomingAgreement -> ViewState.ChangeAddressInProgress(upcomingAgreement)
        is UpcomingAgreementResult.Error -> ViewState.UpcomingAgreementError(upcomingAgreement)
    }

    private suspend fun getSelfChangeState() = when (val selfChangeEligibility = getSelfChangeEligibility()) {
        SelfChangeEligibilityResult.Eligible -> ViewState.SelfChangeAddress
        is SelfChangeEligibilityResult.Blocked -> ViewState.ManualChangeAddress
        is SelfChangeEligibilityResult.Error -> ViewState.SelfChangeError(selfChangeEligibility)
    }

    fun reload() {
        fetchDataAndCreateState()
    }

    sealed class ViewState {
        object Loading : ViewState()
        object SelfChangeAddress : ViewState()
        object ManualChangeAddress : ViewState()
        data class UpcomingAgreementError(val error: UpcomingAgreementResult.Error) : ViewState()
        data class SelfChangeError(val error: SelfChangeEligibilityResult.Error) : ViewState()
        data class ChangeAddressInProgress(
            val upcomingAgreementResult: UpcomingAgreementResult.UpcomingAgreement
        ) : ViewState()
    }
}
