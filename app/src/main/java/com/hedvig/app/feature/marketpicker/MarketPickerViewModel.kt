package com.hedvig.app.feature.marketpicker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.feature.tracking.TrackingFacade
import com.hedvig.hanalytics.HAnalytics
import kotlinx.coroutines.launch

abstract class MarketPickerViewModel : ViewModel() {
    protected val _pickerState = MutableLiveData<PickerState>()
    val pickerState: LiveData<PickerState> = _pickerState
    abstract fun applyMarketAndReload(market: Market)
    abstract fun applyLanguageAndReload(language: Language)
    abstract fun submit()
}

class MarketPickerViewModelImpl(
    private val marketRepository: MarketRepository,
    private val languageRepository: LanguageRepository,
    private val localeBroadcastManager: LocaleBroadcastManager,
    private val marketManager: MarketManager,
    private val context: Context,
    private val trackingFacade: TrackingFacade,
    private val hAnalytics: HAnalytics,
) : MarketPickerViewModel() {

    override fun applyMarketAndReload(market: Market) {
        _pickerState.value = _pickerState.value?.let {
            updateState(market, market.toLanguage())
            PickerState(market, market.toLanguage())
        }
    }

    override fun applyLanguageAndReload(language: Language) {
        _pickerState.value = _pickerState.value?.let {
            updateState(it.market, language)
            PickerState(it.market, language)
        }
    }

    override fun submit() {
        marketManager.hasSelectedMarket = true
        pickerState.value?.language?.let {
            hAnalytics.marketSelected(it.toString())
            languageRepository.uploadLanguage(it)
        }
    }

    private fun updateState(market: Market, language: Language) {
        persistMarketAndLanguage(market, language)
        broadcastLocale()
        languageRepository.uploadLanguage(language)
    }

    private fun persistMarketAndLanguage(market: Market, language: Language) {
        Language.persist(context, language)
        trackingFacade.setProperty("market", market.name)
        marketManager.market = market
    }

    private fun broadcastLocale() {
        localeBroadcastManager.sendBroadcast()
    }

    init {
        viewModelScope.launch {
            val market = runCatching { marketRepository.getMarket() }.getOrNull() ?: return@launch
            val language = Language.fromSettings(context, market)
            _pickerState.value = PickerState(market, language)
            marketManager.market = market
        }
    }
}

data class PickerState(
    val market: Market,
    val language: Language,
)

sealed class Model {
    data class MarketModel(
        val selection: Market,
    ) : Model()

    data class LanguageModel(
        val selection: Language,
    ) : Model()

    object Button : Model()
}
