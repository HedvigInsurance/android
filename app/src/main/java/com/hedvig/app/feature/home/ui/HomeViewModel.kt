package com.hedvig.app.feature.home.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.graphql.PayinStatusQuery
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.hanalytics.HAnalytics
import com.zhuinden.livedatacombinetuplekt.combineTuple
import e
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

abstract class HomeViewModel(
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    sealed class ViewState {
        data class Success(
            val homeData: HomeQuery.Data,
        ) : ViewState()

        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _homeData = MutableLiveData<ViewState>()
    protected val _payinStatusData = MutableLiveData<PayinStatusQuery.Data>()

    // TODO Fetch address change in progress
    protected val _addressChangeInProgress = MutableLiveData("")

    val data: LiveData<Triple<ViewState?, PayinStatusQuery.Data?, String?>> = combineTuple(
        _homeData,
        _payinStatusData,
        _addressChangeInProgress
    ).distinctUntilChanged()

    abstract fun load()
    abstract fun reload()

    fun onClaimDetailCardClicked(claimId: String) {
        val claim = getClaimById(claimId) ?: return

        hAnalytics.claimCardClick(claimId, claim.status.rawValue)
    }

    fun onClaimDetailCardShown(claimId: String) {
        val claim = getClaimById(claimId) ?: return

        hAnalytics.claimCardVisible(claimId, claim.status.rawValue)
    }

    private fun getClaimById(claimId: String): HomeQuery.Claim? =
        (_homeData.value as? ViewState.Success)
            ?.homeData
            ?.claimStatusCards
            ?.firstOrNull { it.id == claimId }
            ?.claim

    fun onPaymentCardShown() {
        hAnalytics.homePaymentCardVisible()
    }
}

class HomeViewModelImpl(
    private val getHomeUseCase: GetHomeUseCase,
    private val payinStatusRepository: PayinStatusRepository,
    hAnalytics: HAnalytics,
) : HomeViewModel(hAnalytics) {
    init {
        load()
        payinStatusRepository
            .payinStatus()
            .onEach { response ->
                response.data?.let { _payinStatusData.postValue(it) }
            }
            .catch { err ->
                e(err)
            }.launchIn(viewModelScope)
    }

    override fun load() {
        viewModelScope.launch {
            createViewState(forceReload = false)
        }
    }

    override fun reload() {
        viewModelScope.launch {
            createViewState(forceReload = true)
        }
    }

    private suspend fun createViewState(forceReload: Boolean) {
        _homeData.value = ViewState.Loading
        val viewState = when (val result = getHomeUseCase.invoke(forceReload)) {
            is GetHomeUseCase.HomeResult.Error -> ViewState.Error
            is GetHomeUseCase.HomeResult.Home -> ViewState.Success(result.home)
        }
        _homeData.postValue(viewState)

        runCatching { payinStatusRepository.refreshPayinStatus() }
    }
}
