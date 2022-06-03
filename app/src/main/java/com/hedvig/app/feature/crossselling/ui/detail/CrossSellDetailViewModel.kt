package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.core.Either
import com.hedvig.app.feature.crossselling.model.NavigateChat
import com.hedvig.app.feature.crossselling.model.NavigateEmbark
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.hanalytics.AppScreen
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CrossSellDetailViewModel(
    private val crossSellAction: CrossSellData.Action,
    hAnalytics: HAnalytics,
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {

    private val _viewState = MutableStateFlow(ViewState())
    val viewState = _viewState.asStateFlow()

    init {
        hAnalytics.screenView(AppScreen.CROSS_SELL_DETAIL)
    }

    data class ViewState(
        val navigateEmbark: NavigateEmbark? = null,
        val navigateChat: NavigateChat? = null,
        val errorMessage: String? = null,
        val loading: Boolean = false,
    )

    fun onCtaClick() {
        viewModelScope.launch {
            when (val action = crossSellAction) {
                CrossSellData.Action.Chat -> _viewState.value = ViewState(navigateChat = NavigateChat)
                is CrossSellData.Action.Embark -> _viewState.value = createQuoteCartViewState(action)
            }
        }
    }

    private suspend fun createQuoteCartViewState(action: CrossSellData.Action.Embark): ViewState {
        return when (val result = action.createEmbarkStoryIdWithQuoteCart(createQuoteCartUseCase)) {
            is Either.Left -> ViewState(errorMessage = result.value.message)
            is Either.Right -> ViewState(
                navigateEmbark = NavigateEmbark(
                    result.value,
                    action.title
                )
            )
        }
    }

    fun actionOpened() {
        _viewState.value = ViewState()
    }

    fun dismissError() {
        _viewState.update {
            it.copy(errorMessage = null)
        }
    }
}
