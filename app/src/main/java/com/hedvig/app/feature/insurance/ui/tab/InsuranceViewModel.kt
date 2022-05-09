package com.hedvig.app.feature.insurance.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import e
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsuranceViewModel(
    private val getContractsUseCase: GetContractsUseCase,
    private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {
    sealed class ViewState {
        data class Success(
            val items: List<InsuranceModel>,
            val action: CrossSellData.Action?
        ) : ViewState()

        object Loading : ViewState()
        object Error : ViewState()
    }

    protected val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState = _viewState.asStateFlow()
    fun load() {
        viewModelScope.launch {
            _viewState.value = ViewState.Loading
            when (val result = getContractsUseCase.invoke()) {
                is Either.Left -> {
                    result.value.message?.let { e { it } }
                    _viewState.value = ViewState.Error
                }
                is Either.Right -> {
                    val showNotificationBadge = crossSellNotificationBadgeService
                        .getUnseenCrossSells(CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard)
                        .first()
                        .isNotEmpty()
                    val items = items(
                        data = result.value,
                        showCrossSellNotificationBadge = showNotificationBadge
                    )
                    _viewState.value = ViewState.Success(items, null)
                }
            }
        }
    }

    fun markCardCrossSellsAsSeen() {
        viewModelScope.launch {
            crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(
                CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard
            )
        }
    }

    fun onClickCrossSell(action: CrossSellData.Action) {
        when (action) {
            CrossSellData.Action.Chat -> _viewState.update { viewState ->
                when (viewState) {
                    ViewState.Error, ViewState.Loading -> viewState
                    is ViewState.Success -> viewState.copy(action = action)
                }
            }
            is CrossSellData.Action.Embark -> {
                viewModelScope.launch {
                    createQuoteCartUseCase.invoke().tap { quoteCartId ->
                        _viewState.update { viewState ->
                            when (viewState) {
                                ViewState.Error, ViewState.Loading -> viewState
                                is ViewState.Success -> viewState.copy(
                                    action = action.copy(
                                        embarkStoryId = appendQuoteCartId(action.embarkStoryId, quoteCartId.id)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun crossSellActionOpened() {
        _viewState.update { viewState ->
            when (viewState) {
                ViewState.Error, ViewState.Loading -> viewState
                is ViewState.Success -> {
                    viewState.copy(action = null)
                }
            }
        }
    }
}
