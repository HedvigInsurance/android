package com.hedvig.app.feature.insurance.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.service.badge.NotificationBadge
import com.hedvig.app.service.badge.NotificationBadgeService
import com.hedvig.app.service.badge.Seen
import com.hedvig.app.service.badge.isSeen
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

abstract class InsuranceViewModel : ViewModel() {
    sealed class ViewState {
        data class Success(val items: List<InsuranceModel>) : ViewState()
        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _data = MutableStateFlow<ViewState>(ViewState.Loading)
    val data = _data.asStateFlow()
    abstract fun load()
    abstract fun markCrossSellsAsSeen()
}

class InsuranceViewModelImpl(
    private val getContractsUseCase: GetContractsUseCase,
    private val notificationBadgeService: NotificationBadgeService,
) : InsuranceViewModel() {

    override fun load() {
        viewModelScope.launch {
            _data.value = ViewState.Loading
            when (val result = getContractsUseCase.invoke()) {
                is GetContractsUseCase.InsuranceResult.Error -> {
                    result.message?.let { e { it } }
                    _data.value = ViewState.Error
                }
                is GetContractsUseCase.InsuranceResult.Insurance -> {
                    val showNotificationBadge = notificationBadgeService
                        .seenStatus(NotificationBadge.CrossSellInsuranceFragmentCard)
                        .firstOrNull()
                        .isSeen()
                        .not()
                    val items = items(result.insurance, showNotificationBadge)
                    _data.value = ViewState.Success(items)
                }
            }
        }
    }

    override fun markCrossSellsAsSeen() {
        viewModelScope.launch {
            notificationBadgeService.setSeenStatus(NotificationBadge.CrossSellInsuranceFragmentCard, Seen.seen())
        }
    }
}
