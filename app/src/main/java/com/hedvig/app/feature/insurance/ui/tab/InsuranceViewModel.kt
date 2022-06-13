package com.hedvig.app.feature.insurance.ui.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.android.owldroid.graphql.InsuranceQuery
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.app.feature.insurance.data.GetContractsUseCase
import com.hedvig.app.feature.insurance.ui.InsuranceModel
import com.hedvig.app.service.badge.CrossSellNotificationBadgeService
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InsuranceViewModel(
    private val getContractsUseCase: GetContractsUseCase,
    private val crossSellNotificationBadgeService: CrossSellNotificationBadgeService,
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
    private val hAnalytics: HAnalytics,
) : ViewModel() {

    data class ViewState(
        val items: List<InsuranceModel>? = null,
        val navigateEmbark: NavigateEmbark? = null,
        val navigateChat: NavigateChat? = null,
        val errorMessage: String? = null,
        val loading: Boolean = false,
    )

    protected val _viewState = MutableStateFlow(ViewState(loading = true))
    val viewState = _viewState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _viewState.value = ViewState(loading = true)
            when (val result = getContractsUseCase.invoke()) {
                is Either.Left -> _viewState.value = ViewState(errorMessage = result.value.message, items = null)
                is Either.Right -> _viewState.value = ViewState(items = createInsuranceItems(result))
            }
        }
    }

    private suspend fun createInsuranceItems(result: Either.Right<InsuranceQuery.Data>): List<InsuranceModel> {
        val showNotificationBadge = crossSellNotificationBadgeService
            .getUnseenCrossSells(CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard)
            .first()
            .isNotEmpty()

        return items(
            data = result.value,
            showCrossSellNotificationBadge = showNotificationBadge
        )
    }

    fun markCardCrossSellsAsSeen() {
        viewModelScope.launch {
            crossSellNotificationBadgeService.markCurrentCrossSellsAsSeen(
                CrossSellNotificationBadgeService.CrossSellBadgeType.InsuranceFragmentCard
            )
        }
    }

    fun onClickCrossSellCard(data: CrossSellData) {
        hAnalytics.cardClickCrossSellDetail(id = data.typeOfContract)
    }

    fun onClickCrossSellAction(data: CrossSellData) {
        viewModelScope.launch {
            when (val action = data.action) {
                CrossSellData.Action.Chat -> _viewState.update {
                    it.copy(navigateChat = NavigateChat)
                }
                is CrossSellData.Action.Embark -> {
                    hAnalytics.cardClickCrossSellDetail(
                        id = data.typeOfContract,
                        storyName = action.embarkStoryId,
                    )
                    _viewState.value = action.toViewState()
                }
            }
        }
    }

    private suspend fun CrossSellData.Action.Embark.toViewState(): ViewState {
        return when (val result = createQuoteCartUseCase.invoke()) {
            is Either.Left -> _viewState.value.copy(errorMessage = result.value.message)
            is Either.Right -> {
                val embarkStoryId = appendQuoteCartId(embarkStoryId, result.value.id)
                val navigateEmbark = NavigateEmbark(embarkStoryId, title)
                _viewState.value.copy(navigateEmbark = navigateEmbark)
            }
        }
    }

    fun crossSellActionOpened() {
        _viewState.update {
            it.copy(navigateChat = null, navigateEmbark = null)
        }
    }
}
