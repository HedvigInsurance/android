package com.hedvig.app.feature.home.ui.changeaddress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.home.ui.changeaddress.GetAddressChangeStoryIdUseCase.SelfChangeEligibilityResult
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult
import kotlinx.coroutines.launch

abstract class ChangeAddressViewModel : ViewModel() {
    protected val _viewState = MutableLiveData<ViewState>()
    abstract val viewState: LiveData<ViewState>
    abstract fun reload()
}

class ChangeAddressViewModelImpl(
    private val getUpcomingAgreement: GetUpcomingAgreementUseCase,
    private val addressChangeStoryId: GetAddressChangeStoryIdUseCase,
) : ChangeAddressViewModel() {

    override val viewState: LiveData<ViewState>
        get() = _viewState

    init {
        fetchDataAndCreateState()
    }

    private fun fetchDataAndCreateState() {
        _viewState.postValue(ViewState.Loading)
        viewModelScope.launch {
            _viewState.postValue(createViewState())
        }
    }

    private suspend fun createViewState(): ViewState {
        return getUpComingAgreementState(
            onNoUpcomingChange = ::getSelfChangeState
        )
    }

    private suspend fun getUpComingAgreementState(onNoUpcomingChange: suspend () -> ViewState) =
        when (val upcomingAgreement = getUpcomingAgreement()) {
            is UpcomingAgreementResult.NoUpcomingAgreementChange -> onNoUpcomingChange()
            is UpcomingAgreementResult.UpcomingAgreement -> ViewState.ChangeAddressInProgress(upcomingAgreement)
            is UpcomingAgreementResult.Error -> ViewState.UpcomingAgreementError(upcomingAgreement)
        }

    private suspend fun getSelfChangeState() = when (val selfChangeEligibility = addressChangeStoryId()) {
        is SelfChangeEligibilityResult.Eligible -> ViewState.SelfChangeAddress(selfChangeEligibility.embarkStoryId)
        is SelfChangeEligibilityResult.Blocked -> ViewState.ManualChangeAddress
        is SelfChangeEligibilityResult.Error -> ViewState.SelfChangeError(selfChangeEligibility)
    }

    override fun reload() {
        fetchDataAndCreateState()
    }
}

sealed class ViewState {
    object Loading : ViewState()
    data class SelfChangeAddress(val embarkStoryId: String) : ViewState()
    object ManualChangeAddress : ViewState()
    data class UpcomingAgreementError(val error: UpcomingAgreementResult.Error) : ViewState()
    data class SelfChangeError(val error: SelfChangeEligibilityResult.Error) : ViewState()
    data class ChangeAddressInProgress(
        val upcomingAgreementResult: UpcomingAgreementResult.UpcomingAgreement
    ) : ViewState()
}
