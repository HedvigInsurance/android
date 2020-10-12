package com.hedvig.app.feature.marketpicker

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.hedvig.app.feature.settings.Language
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_FI
import com.hedvig.app.testdata.feature.marketpicker.GEO_DATA_SE
import com.hedvig.app.util.extensions.getLanguage
import com.hedvig.app.util.extensions.getMarket

class MockMarketPickerViewModel(
    private val myApplication: Application
) : MarketPickerViewModel(myApplication) {
    override val _data = MutableLiveData<PickerState>()
    override val data: LiveData<PickerState> = _data

    override fun uploadLanguage() {
    }

    init {
        if (myApplication.getMarket() == null) {
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
            myApplication.getMarket()?.let { market ->
                _data.postValue(
                    PickerState(
                        market, myApplication.getLanguage()
                    )
                )
            }
        }
    }

    companion object {
        var AVAILABLE_GEO_MARKET = true
    }
}

