package com.hedvig.app.feature.marketpicker

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

abstract class MarketPickerViewModel : ViewModel() {
    abstract val selectedMarket: LiveData<Market>
    abstract fun updateMarket(market: Market)
}

class MarketPickerViewModelImpl : MarketPickerViewModel() {
    override val selectedMarket = MutableLiveData<Market>()

    override fun updateMarket(market: Market) {
        selectedMarket.postValue(market)
    }
}
