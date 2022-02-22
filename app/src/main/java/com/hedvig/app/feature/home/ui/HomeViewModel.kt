package com.hedvig.app.feature.home.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import arrow.core.zip
import com.hedvig.android.owldroid.graphql.HomeQuery
import com.hedvig.android.owldroid.type.PayinMethodStatus
import com.hedvig.app.data.debit.PayinStatusRepository
import com.hedvig.app.feature.home.data.GetHomeUseCase
import com.hedvig.app.feature.home.model.HomeItemsBuilder
import com.hedvig.app.feature.home.model.HomeModel
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

abstract class HomeViewModel(
    private val hAnalytics: HAnalytics,
) : ViewModel() {
    sealed class ViewState {
        data class Success(
            val homeData: HomeQuery.Data,
            val homeItems: List<HomeModel>
        ) : ViewState()

        data class Error(val message: String?) : ViewState()
        object Loading : ViewState()
    }

    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewState

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
        (_viewState.value as? ViewState.Success)
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
    private val homeItemsBuilder: HomeItemsBuilder,
    hAnalytics: HAnalytics,
) : HomeViewModel(hAnalytics) {
    init {
        load()
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
        _viewState.value = ViewState.Loading

        val result = getHomeUseCase.invoke(forceReload)
            .zip(payinStatusRepository.payinStatus())

        _viewState.value = when (result) {
            is Either.Left -> ViewState.Error(result.value.message)
            is Either.Right -> ViewState.Success(
                homeData = result.value.first,
                homeItems = homeItemsBuilder.buildItems(
                    homeData = result.value.first,
                    needsPayinSetup = result.value.second.payinMethodStatus == PayinMethodStatus.NEEDS_SETUP
                )
            )
        }

        runCatching { payinStatusRepository.refreshPayinStatus() }
    }
}
