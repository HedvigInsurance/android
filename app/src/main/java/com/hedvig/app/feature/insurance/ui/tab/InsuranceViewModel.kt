package com.hedvig.app.feature.insurance.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val items: List<InsuranceModel>) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()
    abstract fun load()
    abstract fun markCardCrossSellsAsSeen()
}

class InsuranceViewModelImpl(
    private val getContractsUseCase: GetContractsUseCase,
    private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
) : InsuranceViewModel() {

    override fun load() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            when (val result = getContractsUseCase.invoke()) {
                is GetContractsUseCase.InsuranceResult.Error -> {
                    result.message?.let { e { it } }
                    _viewState.value = ViewState.Error
                }
                is GetContractsUseCase.InsuranceResult.Insurance -> {
                    val showNotificationBadge = crossSellNotificationBadgeService
                        .getUnseenCrossSells(CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard)
                        .first()
                        .isNotEmpty()
                    val items = items(result.insurance, showNotificationBadge)
                    _viewState.value = ViewState.Success(items)
                }
            }
        }
    }

    override fun markCardCrossSellsAsSeen() {
        viewModelScope.launch {
            crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(
                CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard
            )
        }
    }
}
