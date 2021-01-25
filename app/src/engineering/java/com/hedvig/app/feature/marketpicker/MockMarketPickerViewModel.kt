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
                    Market.SE -> _pickerSate.postValue(PickerState(market, Language.EN_SE))
                    Market.NO -> _pickerSate.postValue(PickerState(market, Language.EN_NO))
                    Market.DK -> _pickerSate.postValue(PickerState(market, Language.EN_DK))
                }
            } catch (e: Exception) {
                _pickerSate.postValue(
                    PickerState(
                        Market.SE, Language.EN_SE
                    )
                )
            }
        } else {
            context.getMarket()?.let { market ->
                _pickerSate.postValue(
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
