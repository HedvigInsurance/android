package com.hedvig.app.feature.crossselling.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.crossselling.ui.CrossSellData
import com.hedvig.app.feature.embark.quotecart.CreateQuoteCartUseCase
import com.hedvig.app.feature.home.ui.changeaddress.appendQuoteCartId
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CrossSellDetailViewModel(
    private val crossSell: CrossSellData,
    hAnalytics: HAnalytics,
    private val createQuoteCartUseCase: CreateQuoteCartUseCase,
) : ViewModel() {
    private val _action = MutableStateFlow<CrossSellData.Action?>(null)
    val action = _action.asStateFlow()

    init {
        hAnalytics.screenViewCrossSellDetail(crossSell.typeOfContract)
    }

    fun onCtaClick() {
        when (crossSell.action) {
            is CrossSellData.Action.Embark -> {
                viewModelScope.launch {
                    createQuoteCartUseCase.invoke().tap { quoteCartId ->
                        _action.value = crossSell.action.copy(
                            embarkStoryId = appendQuoteCartId(
                                crossSell.action.embarkStoryId,
                                quoteCartId.id,
                            )
                        )
                    }
                }
            }
            CrossSellData.Action.Chat -> _action.value = crossSell.action
        }
    }

    fun actionOpened() {
        _action.value = null
    }
}
