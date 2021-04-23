package com.hedvig.app.feature.home.ui.changeaddress

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.home.ui.changeaddress.GetSelfChangeEligibilityUseCase.SelfChangeEligibilityResult
import com.hedvig.app.feature.home.ui.changeaddress.GetUpcomingAgreementUseCase.UpcomingAgreementResult
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ChangeAddressInProgress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.Loading
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.ManualChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeAddress
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.SelfChangeError
import com.hedvig.app.feature.home.ui.changeaddress.ViewState.UpcomingAgreementError
import kotlinx.coroutines.launch

abstract class ChangeAddressViewModel : ViewModel() {
    protected val _viewState = MutableLiveData<ViewState>()
    abstract val viewState: LiveData<ViewState>
    abstract fun reload()
}

class ChangeAddressViewModelImpl(
    private val getUpcomingAgreement: GetUpcomingAgreementUseCase,
    private val getSelfChangeEligibility: GetSelfChangeEligibilityUseCase,
) : ChangeAddressViewModel() {

    override val viewState: LiveData<ViewState>
        get() = _viewState

    init {
        fetchDataAndCreateState()
    }

    private fun fetchDataAndCreateState() {
        _viewState.postValue(Loading)
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
            is UpcomingAgreementResult.UpcomingAgreement -> ChangeAddressInProgress(upcomingAgreement)
            is UpcomingAgreementResult.Error -> UpcomingAgreementError(upcomingAgreement)
        }

    private suspend fun getSelfChangeState() = when (val selfChangeEligibility = getSelfChangeEligibility()) {
        is SelfChangeEligibilityResult.Eligible -> SelfChangeAddress(selfChangeEligibility.embarkStoryId)
        is SelfChangeEligibilityResult.Blocked -> ManualChangeAddress
        is SelfChangeEligibilityResult.Error -> SelfChangeError(selfChangeEligibility)
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
        val upcomingAgreementResult: UpcomingAgreementResult.UpcomingAgreement,
    ) : ViewState()
}
