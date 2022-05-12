package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.feature.settings.Market
import com.hedvig.app.feature.settings.MarketManager
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.extensions.getLanguage

class MockMarketPickerViewModel(context: Context, marketManager: MarketManager) : MarketPickerViewModel() {
    override fun applyMarketAndReload(market: Market) {
        _pickerState.value = _pickerState.value?.let {
            PickerState(market, it.language)
        }
    }

    override fun applyLanguageAndReload(language: Language) {
        _pickerState.value = _pickerState.value?.let {
            PickerState(it.market, language)
        }
    }

    override fun submit() = Unit

    init {
        if (marketManager.market == null) {
            try {
                val market: Market = if (AVAILABLE_GEO_MARKET) {
                    Market.valueOf(GEO_DATA_SE.geo.countryISOCode)
                } else {
                    Market.valueOf(GEO_DATA_FI.geo.countryISOCode)
                }
                when (market) {
                    Market.SE -> _pickerState.postValue(PickerState(market, Language.EN_SE))
                    Market.NO -> _pickerState.postValue(PickerState(market, Language.EN_NO))
                    Market.DK -> _pickerState.postValue(PickerState(market, Language.EN_DK))
                    Market.FR -> {}
                }
            } catch (e: Exception) {
                _pickerState.postValue(
                    PickerState(
                        Market.SE, Language.EN_SE
                    )
                )
            }
        } else {
            marketManager.market?.let { market ->
                _pickerState.postValue(
                    PickerState(
                        market, context.getLanguage() ?: market.toLanguage()
                    )
                )
            }
        }
    }

    companion object {
        var AVAILABLE_GEO_MARKET = true
    }
}
