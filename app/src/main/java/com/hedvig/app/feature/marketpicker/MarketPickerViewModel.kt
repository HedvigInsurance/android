package com.hedvig.app.feature.marketpicker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel : ViewModel() {
    protected val _pickerSate = MutableLiveData<PickerState>()
    val pickerState: LiveData<PickerState> = _pickerSate
    abstract fun submitMarketAndReload(market: Market)
    abstract fun submitLanguageAndReload(language: Language)
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val localeBroadcastManager: LocaleBroadcastManager,
    private val marketManager: MarketManager,
    private val context: Context
) : MarketPickerViewModel() {

    override fun submitMarketAndReload(market: Market) {
        _pickerSate.value = _pickerSate.value?.let {
            updateState(market, market.toLanguage())
            PickerState(market, market.toLanguage())
        }
    }

    override fun submitLanguageAndReload(language: Language) {
        _pickerSate.value = _pickerSate.value?.let {
            updateState(it.market, language)
            PickerState(it.market, language)
        }
    }

    private fun updateState(market: Market, language: Language) {
        persistMarketAndLanguage(market, language)
        broadcastLocale()
        languageRepository.uploadLanguage(language)
    }

    private fun persistMarketAndLanguage(market: Market, language: Language) {
        Language.persist(context, language)
        marketManager.market = market
    }

    private fun broadcastLocale() {
        localeBroadcastManager.sendBroadcast()
    }

    init {
        viewModelScope.launch {
            val market = marketRepository.getMarket()
            _pickerSate.value = PickerState(market, market.toLanguage())
            marketManager.market = market
        }
    }
}

data class PickerState(
    val market: Market,
    val language: Language
)

sealed class Model {
    data class MarketModel(
        val selection: Market
    ) : Model()

    data class LanguageModel(
        val selection: Language
    ) : Model()

    object Button : Model()
}
