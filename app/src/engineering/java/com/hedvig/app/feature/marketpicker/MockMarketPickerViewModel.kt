package com.hedvig.app.feature.marketpicker

import android.content.Context
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket

class MockMarketPickerViewModel(
    private val context: Context
) : MarketPickerViewModel(context) {

    override fun uploadLanguage() {
    }

    init {
        if (context.getMarket() == null) {
            val market: Market
            try {
                market = if (AVAILABLE_GEO_MARKET) {
                    Market.valueOf(GEO_DATA_SE.geo.countryISOCode)
                } else {
                    Market.valueOf(GEO_DATA_FI.geo.countryISOCode)
                }
                when (market) {
                    Market.SE -> _data.postValue(PickerState(market, Language.EN_SE))
                    Market.NO -> _data.postValue(PickerState(market, Language.EN_NO))
                }
            } catch (e: Exception) {
                _data.postValue(
                    PickerState(
                        Market.SE, Language.EN_SE
                    )
                )
            }
        } else {
            context.getMarket()?.let { market ->
                _data.postValue(
                    PickerState(
                        market, context.getLanguage()
                    )
                )
            }
        }
    }

    companion object {
        var AVAILABLE_GEO_MARKET = true
    }
}

